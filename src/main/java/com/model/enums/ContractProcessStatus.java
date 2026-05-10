package com.model.enums;

public enum ContractProcessStatus {
    DENIED,
    IN_PROGRESS,
    SUCCEEDED;

    public static boolean invalid(String status) {
        return status.equals(ContractProcessStatus.IN_PROGRESS.name()) ||
                status.equals(ContractProcessStatus.SUCCEEDED.name()) ||
                status.equals(ContractProcessStatus.DENIED.name());
    }
}
