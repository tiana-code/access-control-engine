package com.accesscontrol.service;

import com.accesscontrol.dto.request.StartWorkflowRequest;
import com.accesscontrol.dto.request.WorkflowTransitionRequest;
import com.accesscontrol.dto.response.WorkflowResponse;
import com.accesscontrol.exception.InvalidTransitionException;
import com.accesscontrol.model.ApprovalWorkflow;
import com.accesscontrol.model.enums.WorkflowAction;
import com.accesscontrol.model.enums.WorkflowState;
import com.accesscontrol.repository.ApprovalWorkflowRepository;
import com.accesscontrol.service.impl.WorkflowEngineImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowEngineImplTest {

    @Mock
    private ApprovalWorkflowRepository workflowRepository;

    @InjectMocks
    private WorkflowEngineImpl workflowEngine;

    @Test
    void start_createsWorkflowInPendingReviewState() {
        UUID resourceId = UUID.randomUUID();
        UUID initiatorId = UUID.randomUUID();
        StartWorkflowRequest request = new StartWorkflowRequest(resourceId, "PurchaseOrder", initiatorId, "test workflow");

        when(workflowRepository.save(any(ApprovalWorkflow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowResponse response = workflowEngine.start(request);

        assertThat(response.state()).isEqualTo(WorkflowState.PENDING_REVIEW);
        assertThat(response.resourceId()).isEqualTo(resourceId);
        assertThat(response.resourceType()).isEqualTo("PurchaseOrder");
        assertThat(response.initiatorId()).isEqualTo(initiatorId);
    }

    @Test
    void transition_withApproveAction_movesToApproved() {
        UUID workflowId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        ApprovalWorkflow workflow = new ApprovalWorkflow(UUID.randomUUID(), "Contract", UUID.randomUUID());
        workflow.transition(WorkflowAction.SUBMIT, actorId, "submitted");

        when(workflowRepository.findByIdWithEvents(workflowId)).thenReturn(Optional.of(workflow));
        when(workflowRepository.save(any(ApprovalWorkflow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowTransitionRequest request = new WorkflowTransitionRequest(WorkflowAction.APPROVE, actorId, null, "approved");

        WorkflowResponse response = workflowEngine.transition(workflowId, request);

        assertThat(response.state()).isEqualTo(WorkflowState.APPROVED);
        assertThat(response.completedAt()).isNotNull();
    }

    @Test
    void transition_fromTerminalState_throws() {
        UUID workflowId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        ApprovalWorkflow workflow = new ApprovalWorkflow(UUID.randomUUID(), "Contract", UUID.randomUUID());
        workflow.transition(WorkflowAction.SUBMIT, actorId, "submitted");
        workflow.transition(WorkflowAction.APPROVE, actorId, "approved");

        when(workflowRepository.findByIdWithEvents(workflowId)).thenReturn(Optional.of(workflow));

        WorkflowTransitionRequest request = new WorkflowTransitionRequest(WorkflowAction.REJECT, actorId, null, "too late");

        assertThatThrownBy(() -> workflowEngine.transition(workflowId, request))
                .isInstanceOf(InvalidTransitionException.class);
    }
}
