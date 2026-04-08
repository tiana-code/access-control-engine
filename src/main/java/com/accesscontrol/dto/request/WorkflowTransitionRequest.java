package com.accesscontrol.dto.request;

import com.accesscontrol.model.enums.WorkflowAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record WorkflowTransitionRequest(
        @NotNull
        WorkflowAction action,

        @NotNull
        UUID actorId,

        UUID assigneeId,

        @Size(max = 2048)
        String comment
) {
}
