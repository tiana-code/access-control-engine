package com.accesscontrol.model;

import com.accesscontrol.model.enums.WorkflowState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprovalWorkflowTest {

    @Test
    void transitionToPendingReviewFromDraft() {
        ApprovalWorkflow workflow = createWorkflow();
        workflow.transition(WorkflowState.PENDING_REVIEW, UUID.randomUUID(), "submitted");

        assertThat(workflow.getState()).isEqualTo(WorkflowState.PENDING_REVIEW);
        assertThat(workflow.getEvents()).hasSize(1);
        assertThat(workflow.getCompletedAt()).isNull();
    }

    @Test
    void transitionToApprovedSetsCompletedAt() {
        ApprovalWorkflow workflow = createWorkflow();
        workflow.transition(WorkflowState.PENDING_REVIEW, UUID.randomUUID(), "submitted");
        workflow.transition(WorkflowState.APPROVED, UUID.randomUUID(), "looks good");

        assertThat(workflow.getState()).isEqualTo(WorkflowState.APPROVED);
        assertThat(workflow.getCompletedAt()).isNotNull();
        assertThat(workflow.getEvents()).hasSize(2);
    }

    @Test
    void cannotTransitionFromTerminalState() {
        ApprovalWorkflow workflow = createWorkflow();
        workflow.transition(WorkflowState.APPROVED, UUID.randomUUID(), "approved");

        assertThatThrownBy(() ->
                workflow.transition(WorkflowState.REJECTED, UUID.randomUUID(), "too late")
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminal state");
    }

    @Test
    void eventRecordsFromAndToState() {
        ApprovalWorkflow workflow = createWorkflow();
        workflow.transition(WorkflowState.PENDING_REVIEW, UUID.randomUUID(), "submit");

        WorkflowEvent event = workflow.getEvents().getFirst();
        assertThat(event.getFromState()).isEqualTo(WorkflowState.DRAFT);
        assertThat(event.getToState()).isEqualTo(WorkflowState.PENDING_REVIEW);
    }

    @Test
    void escalateThenApprove() {
        ApprovalWorkflow workflow = createWorkflow();
        workflow.transition(WorkflowState.PENDING_REVIEW, UUID.randomUUID(), "submit");
        workflow.transition(WorkflowState.ESCALATED, UUID.randomUUID(), "need senior review");
        workflow.transition(WorkflowState.APPROVED, UUID.randomUUID(), "approved by senior");

        assertThat(workflow.getState()).isEqualTo(WorkflowState.APPROVED);
        assertThat(workflow.getEvents()).hasSize(3);
        assertThat(workflow.getCompletedAt()).isNotNull();
    }

    private ApprovalWorkflow createWorkflow() {
        return new ApprovalWorkflow(UUID.randomUUID(), "PurchaseOrder", UUID.randomUUID());
    }
}
