package com.accesscontrol.model;

import com.accesscontrol.model.enums.WorkflowState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowStateTest {

    @Test
    void approvedIsTerminal() {
        assertThat(WorkflowState.APPROVED.isTerminal()).isTrue();
    }

    @Test
    void rejectedIsTerminal() {
        assertThat(WorkflowState.REJECTED.isTerminal()).isTrue();
    }

    @Test
    void cancelledIsTerminal() {
        assertThat(WorkflowState.CANCELLED.isTerminal()).isTrue();
    }

    @Test
    void draftIsNotTerminal() {
        assertThat(WorkflowState.DRAFT.isTerminal()).isFalse();
    }

    @Test
    void pendingReviewIsNotTerminal() {
        assertThat(WorkflowState.PENDING_REVIEW.isTerminal()).isFalse();
    }

    @Test
    void escalatedIsNotTerminal() {
        assertThat(WorkflowState.ESCALATED.isTerminal()).isFalse();
    }
}
