package com.accesscontrol.service;

import com.accesscontrol.model.enums.WorkflowAction;
import com.accesscontrol.model.enums.WorkflowState;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowTransitionsTest {

    @Test
    void submitFromDraftLeadsToPendingReview() {
        Optional<WorkflowState> next = WorkflowTransitions.resolve(WorkflowState.DRAFT, WorkflowAction.SUBMIT);
        assertThat(next).contains(WorkflowState.PENDING_REVIEW);
    }

    @Test
    void approveNotAllowedFromDraft() {
        Optional<WorkflowState> next = WorkflowTransitions.resolve(WorkflowState.DRAFT, WorkflowAction.APPROVE);
        assertThat(next).isEmpty();
    }

    @Test
    void cancelAllowedFromDraft() {
        Optional<WorkflowState> next = WorkflowTransitions.resolve(WorkflowState.DRAFT, WorkflowAction.CANCEL);
        assertThat(next).contains(WorkflowState.CANCELLED);
    }

    @Test
    void approveAllowedFromPendingReview() {
        Optional<WorkflowState> next = WorkflowTransitions.resolve(WorkflowState.PENDING_REVIEW, WorkflowAction.APPROVE);
        assertThat(next).contains(WorkflowState.APPROVED);
    }

    @Test
    void escalateAllowedFromPendingReview() {
        Optional<WorkflowState> next = WorkflowTransitions.resolve(WorkflowState.PENDING_REVIEW, WorkflowAction.ESCALATE);
        assertThat(next).contains(WorkflowState.ESCALATED);
    }

    @Test
    void approveAllowedFromEscalated() {
        Optional<WorkflowState> next = WorkflowTransitions.resolve(WorkflowState.ESCALATED, WorkflowAction.APPROVE);
        assertThat(next).contains(WorkflowState.APPROVED);
    }

    @Test
    void noTransitionsFromTerminalStates() {
        assertThat(WorkflowTransitions.allowedActions(WorkflowState.APPROVED)).isEmpty();
        assertThat(WorkflowTransitions.allowedActions(WorkflowState.REJECTED)).isEmpty();
        assertThat(WorkflowTransitions.allowedActions(WorkflowState.CANCELLED)).isEmpty();
    }

    @Test
    void pendingReviewHasAllExpectedActions() {
        Set<WorkflowAction> actions = WorkflowTransitions.allowedActions(WorkflowState.PENDING_REVIEW);
        assertThat(actions).containsExactlyInAnyOrder(
                WorkflowAction.APPROVE, WorkflowAction.REJECT,
                WorkflowAction.ESCALATE, WorkflowAction.CANCEL);
    }
}
