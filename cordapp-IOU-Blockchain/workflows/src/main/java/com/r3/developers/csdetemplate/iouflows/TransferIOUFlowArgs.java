package com.r3.developers.csdetemplate.iouflows;

import java.util.UUID;

// A class to hold the deserialized arguments required to start the flow.
public class TransferIOUFlowArgs {

    // Serialisation service requires a default constructor
    public TransferIOUFlowArgs() {}

    private String newLender;
    private UUID iouId;

    public TransferIOUFlowArgs(String newLender, UUID iouId) {
        this.newLender = newLender;
        this.iouId = iouId;
    }

    public String getNewLender() {
        return newLender;
    }

    public UUID getIouId() {
        return iouId;
    }


}