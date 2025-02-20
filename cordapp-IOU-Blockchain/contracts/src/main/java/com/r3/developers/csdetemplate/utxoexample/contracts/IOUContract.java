package com.r3.developers.csdetemplate.utxoexample.contracts;

import com.r3.developers.csdetemplate.utxoexample.states.IOUState;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Set;

import static java.util.Objects.requireNonNull;

// todo: START here

public class IOUContract implements Contract {

    private final static Logger log = LoggerFactory.getLogger(IOUContract.class);

    public static class Issue implements Command { }
    public static class Settle implements Command { }
    public static class Transfer implements Command { }

    @Override
    public boolean isRelevant(@NotNull ContractState state, @NotNull Set<? extends PublicKey> myKeys) {
        return Contract.super.isRelevant(state, myKeys);
    }

    @Override
    public void verify(UtxoLedgerTransaction transaction) {

        requireThat( transaction.getCommands().size() == 1, "Require a single command.");
        Command command = transaction.getCommands().get(0);

        IOUState output = transaction.getOutputStates(IOUState.class).get(0);

        requireThat(output.getParticipants().size() == 2, "The output state should have two and only two participants.");

        if(command.getClass() == Issue.class) {
            requireThat(transaction.getInputContractStates().isEmpty(), "When command is Issue there should be no input states.");
            requireThat(transaction.getOutputContractStates().size() == 1, "When command is Issue there should be one and only one output state.");
            requireThat(transaction.getOutputContractStates().get(0) instanceof IOUState, "When command is Issue the output state must be an IOUState.");
        }
        else if(command.getClass() == Settle.class) {
            requireThat(transaction.getInputContractStates().size() == 1, "When command is Settle there should be one and only one input state.");
            requireThat(transaction.getInputContractStates().get(0) instanceof IOUState, "When command is Settle the input state must be an IOUState.");
            requireThat(transaction.getOutputContractStates().size() == 1, "When command is Settle there should be one and only one output state.");
            IOUState input = transaction.getInputStates(IOUState.class).get(0);
            requireThat(input.getId().equals(output.getId()), "When command is Settle id must not change.");
        }
        else if(command.getClass() == Transfer.class) {
            requireThat(transaction.getOutputContractStates().size() == 1, "When command is Transfer there should be one and only one output state.");
        }
        else {
            throw new CordaRuntimeException("Unsupported command");
        }
    }

    private void requireThat(boolean asserted, String errorMessage) {
        if(!asserted) {
            throw new CordaRuntimeException("Failed requirement: " + errorMessage);
        }
    }
}
