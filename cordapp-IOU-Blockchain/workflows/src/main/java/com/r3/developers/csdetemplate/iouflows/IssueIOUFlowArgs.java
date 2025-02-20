package com.r3.developers.csdetemplate.iouflows;

// A class to hold the deserialized arguments required to start the flow.
public class IssueIOUFlowArgs {

    // Serialisation service requires a default constructor
    public IssueIOUFlowArgs() {}

    private String lender;
    private String amount;

    public IssueIOUFlowArgs(String amount, String lender) {
        this.amount = amount;
        this.lender = lender;
    }

    public String getAmount() {
        return amount;
    }

    public String getLender() {
        return lender;
    }


}