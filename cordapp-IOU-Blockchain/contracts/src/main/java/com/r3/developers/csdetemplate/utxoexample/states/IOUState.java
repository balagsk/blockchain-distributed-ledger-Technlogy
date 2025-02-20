package com.r3.developers.csdetemplate.utxoexample.states;

import com.r3.developers.csdetemplate.utxoexample.contracts.IOUContract;
import net.corda.v5.base.annotations.ConstructorForDeserialization;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.utxo.BelongsToContract;
import net.corda.v5.ledger.utxo.ContractState;

import java.security.PublicKey;
import java.util.*;

//@CordaSerializable
@BelongsToContract(IOUContract.class)
public class IOUState implements ContractState {

    private final UUID id;
    private final MemberX500Name lender;
    private final MemberX500Name borrower;
    private final int amount;
    private final int amountPaid;
    public List<PublicKey> participants;


    // Allows serialisation and to use a specified UUID.
    @ConstructorForDeserialization
    public IOUState(UUID id,
                    MemberX500Name lender,
                    MemberX500Name borrower,
                    int amount,
                    int amountPaid,
                    List<PublicKey> participants) {
        this.id = id;
        this.lender = lender;
        this.borrower = borrower;
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.participants = participants;
    }
    public IOUState(UUID id,
                    MemberX500Name lender,
                    MemberX500Name borrower,
                    int amount,
                    List<PublicKey> participants) {
        this.id = id;
        this.lender = lender;
        this.borrower = borrower;
        this.amount = amount;
        this.amountPaid = 0;
        this.participants = participants;
    }

    public UUID getId() {
        return id;
    }
    public MemberX500Name getLender() {
        return lender;
    }
    public MemberX500Name getBorrower() {
        return borrower;
    }
    public int getAmount() {
        return amount;
    }
    public int getAmountPaid() {
        return amountPaid;
    }
    //    @NotNull
//    @Override
    public List<PublicKey> getParticipants() {
        return participants;
    }


    public IOUState processPayment(int amountRepayment) {
        int updatedAmountPaid = this.amountPaid + (amountRepayment);
        return new IOUState(this.id, lender, borrower, amount, updatedAmountPaid, this.participants);
    }

    public IOUState withNewLender(MemberX500Name newLender, List<PublicKey> newParticipants){
        return new IOUState(this.id, newLender, borrower, amount, amountPaid, newParticipants);
    }

}