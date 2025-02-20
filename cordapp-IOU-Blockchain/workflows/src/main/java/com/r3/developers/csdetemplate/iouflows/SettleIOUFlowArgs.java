package com.r3.developers.csdetemplate.iouflows;

import java.util.UUID;

// A class to hold the deserialized arguments required to start the flow.
public class SettleIOUFlowArgs {

    // Serialisation service requires a default constructor
    private String amountRepayment;
    private UUID iouId;

    public SettleIOUFlowArgs() {
    }

    public SettleIOUFlowArgs(String amountRepayment, UUID iouId) {
        this.amountRepayment = amountRepayment;
        this.iouId = iouId;
    }

    public String getAmountRepayment() {
        return amountRepayment;
    }

    public UUID getIouId() {
        return iouId;
    }
}