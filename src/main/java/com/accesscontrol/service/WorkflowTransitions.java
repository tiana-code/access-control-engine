package com.accesscontrol.service;

import com.accesscontrol.model.enums.WorkflowAction;
import com.accesscontrol.model.enums.WorkflowState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class WorkflowTransitions {

    private WorkflowTransitions() {
    }

    private static final Map<WorkflowState, Map<WorkflowAction, WorkflowState>> TABLE = Map.of(
            WorkflowState.DRAFT, Map.of(
                    WorkflowAction.SUBMIT, WorkflowState.PENDING_REVIEW,
                    WorkflowAction.CANCEL, WorkflowState.CANCELLED
            ),
            WorkflowState.PENDING_REVIEW, Map.of(
                    WorkflowAction.APPROVE, WorkflowState.APPROVED,
                    WorkflowAction.REJECT, WorkflowState.REJECTED,
                    WorkflowAction.ESCALATE, WorkflowState.ESCALATED,
                    WorkflowAction.CANCEL, WorkflowState.CANCELLED
            ),
            WorkflowState.ESCALATED, Map.of(
                    WorkflowAction.APPROVE, WorkflowState.APPROVED,
                    WorkflowAction.REJECT, WorkflowState.REJECTED,
                    WorkflowAction.CANCEL, WorkflowState.CANCELLED
            )
    );

    public static Optional<WorkflowState> resolve(WorkflowState current, WorkflowAction action) {
        return Optional.ofNullable(TABLE.getOrDefault(current, Map.of()).get(action));
    }

    public static Set<WorkflowAction> allowedActions(WorkflowState state) {
        return TABLE.getOrDefault(state, Map.of()).keySet();
    }
}
