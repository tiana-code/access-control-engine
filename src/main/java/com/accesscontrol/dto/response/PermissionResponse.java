package com.accesscontrol.dto.response;

import com.accesscontrol.model.Permission;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String action,
        String resourceType,
        String description
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getAction(),
                permission.getResourceType(),
                permission.getDescription()
        );
    }
}
