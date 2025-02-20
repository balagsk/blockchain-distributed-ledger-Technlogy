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
import net.corda.v5.ledger.common.NotaryLookup;
import net.corda.v5.ledger.common.Party;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder;
import net.corda.v5.membership.MemberInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
public class SettleIOUFlow implements RPCStartableFlow {

    private final static Logger log = LoggerFactory.getLogger(SettleIOUFlow.class);


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

    @NotNull
    @Override
    @Suspendable
    public String call(@NotNull RPCRequestData requestBody) {
        log.info("SettleIOUFlow.call() called");

        try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
            SettleIOUFlowArgs flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, SettleIOUFlowArgs.class);

            // Get MemberInfos for the Vnode running the flow and the otherMember.
            MemberInfo myInfo = memberLookup.myInfo();
            UUID iouID = flowArgs.getIouId();
            int amountRepayment = Integer.parseInt(flowArgs.getAmountRepayment());

            List<StateAndRef<IOUState>> iouStateAndRefs = ledgerService.findUnconsumedStatesByType(IOUState.class);
            List<StateAndRef<IOUState>> iouStateAndRefsWithId = iouStateAndRefs.stream()
                    .filter(sar -> sar.getState().getContractState().getId().equals(iouID)).collect(toList());

            if (iouStateAndRefsWithId.size() != 1) throw new CordaRuntimeException("Multiple or zero Chat states with id " + iouID + " found");
            StateAndRef<IOUState> iouStateAndRef = iouStateAndRefsWithId.get(0);
            Party notary = iouStateAndRef.getState().getNotary();

            IOUState iouInput = iouStateAndRef.getState().getContractState();

            if (!(myInfo.getName().equals(iouInput.getBorrower()))) throw new CordaRuntimeException("Only IOU borrower can settle the IOU.");

            MemberInfo lenderInfo = requireNonNull(
                    memberLookup.lookup(iouInput.getLender()),
                    "MemberLookup can't find otherMember specified in flow arguments."
            );

            // Create the IOUState from the input arguments and member information.
            IOUState iouOutput = iouInput.processPayment(amountRepayment);

            // Use UTXOTransactionBuilder to build up the draft transaction.
            UtxoTransactionBuilder txBuilder = ledgerService.getTransactionBuilder()
                    .setNotary(notary)
                    .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                    .addInputState(iouStateAndRef.getRef())
                    .addOutputState(iouOutput)
                    .addCommand(new IOUContract.Settle())
                    .addSignatories(iouOutput.getParticipants());

            // Convert the transaction builder to a UTXOSignedTransaction and sign with this Vnode's first Ledger key.
            // Note, toSignedTransaction() is currently a placeholder method, hence being marked as deprecated.
            @SuppressWarnings("DEPRECATION")
            UtxoSignedTransaction signedTransaction = txBuilder.toSignedTransaction(myInfo.getLedgerKeys().get(0));

            // Call FinalizeIOUSubFlow which will finalise the transaction.
            // If successful the flow will return a String of the created transaction id,
            // if not successful it will return an error message.
            return flowEngine.subFlow(new FinalizeIOUFlow(signedTransaction, lenderInfo.getName()));
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
