package com.accesscontrol.dto.response;

import com.accesscontrol.model.ResourcePermission;

import java.time.Instant;
import java.util.UUID;

public record ResourcePermissionResponse(
        UUID id,
        UUID userId,
        UUID roleId,
        String roleName,
        UUID permissionId,
        String permissionAction,
        String permissionResourceType,
        UUID resourceId,
        String resourceType,
        Instant grantedAt,
        UUID grantedBy,
        Instant expiresAt,
        boolean expired
) {
    public static ResourcePermissionResponse from(ResourcePermission resourcePermission) {
        return new ResourcePermissionResponse(
                resourcePermission.getId(),
                resourcePermission.getUserId(),
                resourcePermission.getRole() != null ? resourcePermission.getRole().getId() : null,
                resourcePermission.getRole() != null ? resourcePermission.getRole().getName() : null,
                resourcePermission.getPermission().getId(),
                resourcePermission.getPermission().getAction(),
                resourcePermission.getPermission().getResourceType(),
                resourcePermission.getResourceId(),
                resourcePermission.getResourceType(),
                resourcePermission.getGrantedAt(),
                resourcePermission.getGrantedBy(),
                resourcePermission.getExpiresAt(),
                resourcePermission.isExpired()
        );
    }
}
