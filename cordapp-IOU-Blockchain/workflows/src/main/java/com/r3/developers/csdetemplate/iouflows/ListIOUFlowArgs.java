package com.r3.developers.csdetemplate.iouflows;

import java.util.UUID;

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
public class ListIOUFlowArgs {

    private UUID id;
    private String lender;
    private String borrower;
    private int amount;
    private int amountPaid;

    public ListIOUFlowArgs() {
    }

    public ListIOUFlowArgs(UUID id, String lender, String borrower, int amount, int amountPaid) {
        this.id = id;
        this.lender = lender;
        this.borrower = borrower;
        this.amount = amount;
        this.amountPaid = amountPaid;
    }

    public UUID getId() {
        return id;
    }

    public String getLender() {
        return lender;
    }

    public String getBorrower() {
        return borrower;
    }

    public int getAmount() {
        return amount;
    }

    public int getAmountPaid() {
        return amountPaid;
    }
}
/*
RequestBody for triggering the flow via http-rpc:
{
    "clientRequestId": "list-1",
    "flowClassName": "com.r3.developers.csdetemplate.utxoexample.workflows.ListChatsFlow",
    "requestData": {}
}
*/