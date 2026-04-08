package com.accesscontrol.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StartWorkflowRequest(
        @NotNull
        UUID resourceId,

        @NotBlank @Size(max = 128)
        String resourceType,

        @NotNull
        UUID initiatorId,

        @Size(max = 2048)
        String description
) {
}
