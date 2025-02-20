package com.r3.developers.csdetemplate.iouflows;

import com.r3.developers.csdetemplate.utxoexample.contracts.IOUContract;
import com.r3.developers.csdetemplate.utxoexample.states.IOUState;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.FlowEngine;
import net.corda.v5.application.flows.RPCRequestData;
import net.corda.v5.application.flows.RPCStartableFlow;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.application.membership.MemberLookup;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.common.NotaryLookup;
import net.corda.v5.ledger.common.Party;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder;
import net.corda.v5.membership.MemberInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
public class TransferIOUFlow implements RPCStartableFlow {

    private final static Logger log = LoggerFactory.getLogger(TransferIOUFlow.class);

    @CordaInject
    public JsonMarshallingService jsonMarshallingService;

    @CordaInject
    public MemberLookup memberLookup;

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    public UtxoLedgerService ledgerService;

    @CordaInject
    public NotaryLookup notaryLookup;

    // FlowEngine service is required to run SubFlows.
    @CordaInject
    public FlowEngine flowEngine;


    @Suspendable
    @Override
    public String call( RPCRequestData requestBody) {

        log.info("TransferIOUFlow.call() called");

        try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
            TransferIOUFlowArgs flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, TransferIOUFlowArgs.class);

            // Get MemberInfos for the Vnode running the flow and the new lender.
            MemberInfo myInfo = memberLookup.myInfo();
            MemberInfo newLenderLookup = requireNonNull(
                    memberLookup.lookup(MemberX500Name.parse(flowArgs.getNewLender())),
                    "MemberLookup can't find the lender specified in flow arguments."
            );

            // Find the IOUState using iouId.
            UUID iouId = flowArgs.getIouId();
            List<StateAndRef<IOUState>> iouStateAndRefs = ledgerService.findUnconsumedStatesByType(IOUState.class);
            List<StateAndRef<IOUState>> iouStateAndRefsWithId = iouStateAndRefs.stream()
                    .filter(stateAndRefs -> stateAndRefs.getState().getContractState().getId().equals(iouId)).collect(toList());

            if (iouStateAndRefsWithId.size() != 1) throw new CordaRuntimeException(
                    "Multiple or zero IOU states with id " + iouId + " found"
            );
            StateAndRef<IOUState> iouStateAndRef = iouStateAndRefsWithId.get(0);
            IOUState iouInput = iouStateAndRef.getState().getContractState();

            MemberInfo borrower = requireNonNull(
                    memberLookup.lookup(iouInput.getBorrower()),
                    "MemberLookup can't find borrower specified in flow arguments."
            );
//            // Create the IOUState from the input arguments and member information.
            IOUState iouOutput = iouInput.withNewLender(
                    newLenderLookup.getName(),
                    Arrays.asList(borrower.getLedgerKeys().get(0), newLenderLookup.getLedgerKeys().get(0))
            );

            // Obtain the Notary name and public key.
            Party notary = iouStateAndRef.getState().getNotary();
            // Note, in Java CorDapps only unchecked RuntimeExceptions can be thrown not
            // declared checked exceptions as this changes the method signature and breaks override.

            // Use UTXOTransactionBuilder to build up the draft transaction.
            UtxoTransactionBuilder txBuilder = ledgerService.getTransactionBuilder()
                    .setNotary(notary)
                    .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                    .addInputState(iouStateAndRef.getRef())
                    .addOutputState(iouOutput)
                    .addCommand(new IOUContract.Transfer())
                    .addSignatories(iouOutput.getParticipants());

            // Convert the transaction builder to a UTXOSignedTransaction and sign with this Vnode's first Ledger key.
            // Note, toSignedTransaction() is currently a placeholder method, hence being marked as deprecated.
            @SuppressWarnings("DEPRECATION")
            UtxoSignedTransaction signedTransaction = txBuilder.toSignedTransaction(myInfo.getLedgerKeys().get(0));

            // Call FinalizeChatSubFlow which will finalise the transaction.
            // If successful the flow will return a String of the created transaction id,
            // if not successful it will return an error message.
            return flowEngine.subFlow(new FinalizeIOUFlow(signedTransaction, newLenderLookup.getName()));
        }
        // Catch any exceptions, log them and rethrow the exception.
        catch (Exception e) {
            log.warn("Failed to process utxo flow for request body " + requestBody + " because: " + e.getMessage());
            throw new CordaRuntimeException(e.getMessage());
        }
    }
}

/*
RequestBody for triggering the flow via http-rpc:
{
    "clientRequestId": "create-1",
    "flowClassName": "com.r3.developers.csdetemplate.utxoexample.workflows.CreateNewChatFlow",
    "requestData": {
        "chatName":"Chat with Bob",
        "otherMember":"CN=Bob, OU=Test Dept, O=R3, L=London, C=GB",
        "message": "Hello Bob"
        }
}
 */
