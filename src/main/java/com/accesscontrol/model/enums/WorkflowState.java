package com.accesscontrol.model.enums;

public enum WorkflowState {
    DRAFT,
    PENDING_REVIEW,
    ESCALATED,
    APPROVED,
    REJECTED,
    CANCELLED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }
}
