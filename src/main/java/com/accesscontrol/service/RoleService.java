package com.accesscontrol.service;

import com.accesscontrol.dto.request.CreateRoleRequest;
import com.accesscontrol.dto.response.RoleResponse;
import com.accesscontrol.model.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface RoleService {

    RoleResponse create(CreateRoleRequest request);

    RoleResponse getById(UUID id);

    Page<RoleResponse> list(Pageable pageable);

    RoleResponse assignPermission(UUID roleId, UUID permissionId);

    RoleResponse revokePermission(UUID roleId, UUID permissionId);

    Set<Permission> getEffectivePermissions(UUID roleId);

    void delete(UUID id);
}
