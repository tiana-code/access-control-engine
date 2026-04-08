package com.accesscontrol.dto.response;

import com.accesscontrol.model.ApprovalWorkflow;
import com.accesscontrol.model.enums.WorkflowAction;
import com.accesscontrol.model.enums.WorkflowState;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record WorkflowResponse(
        UUID id,
        UUID resourceId,
        String resourceType,
        WorkflowState state,
        UUID initiatorId,
        UUID currentAssigneeId,
        String description,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        Set<WorkflowAction> allowedActions
) {
    public static WorkflowResponse from(ApprovalWorkflow workflow, Set<WorkflowAction> allowedActions) {
        return new WorkflowResponse(
                workflow.getId(),
                workflow.getResourceId(),
                workflow.getResourceType(),
                workflow.getState(),
                workflow.getInitiatorId(),
                workflow.getCurrentAssigneeId(),
                workflow.getDescription(),
                workflow.getCreatedAt(),
                workflow.getUpdatedAt(),
                workflow.getCompletedAt(),
                allowedActions
        );
    }
}
