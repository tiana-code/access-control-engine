package com.accesscontrol.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record GrantPermissionRequest(
        UUID userId,

        UUID roleId,

        @NotNull
        UUID permissionId,

        UUID resourceId,

        @Size(max = 128)
        String resourceType,

        UUID grantedBy,

        Instant expiresAt
) {
}
