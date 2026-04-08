package com.accesscontrol.dto.response;

import com.accesscontrol.model.Role;

import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        UUID parentRoleId,
        int permissionCount
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getParentRole() != null ? role.getParentRole().getId() : null,
                role.getPermissions().size()
        );
    }
}
