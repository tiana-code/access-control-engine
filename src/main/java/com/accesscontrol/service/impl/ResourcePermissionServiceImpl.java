package com.accesscontrol.service.impl;

import com.accesscontrol.dto.request.GrantPermissionRequest;
import com.accesscontrol.dto.response.ResourcePermissionResponse;
import com.accesscontrol.exception.NotFoundException;
import com.accesscontrol.model.Permission;
import com.accesscontrol.model.ResourcePermission;
import com.accesscontrol.model.Role;
import com.accesscontrol.repository.PermissionRepository;
import com.accesscontrol.repository.ResourcePermissionRepository;
import com.accesscontrol.repository.RoleRepository;
import com.accesscontrol.service.ResourcePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourcePermissionServiceImpl implements ResourcePermissionService {

    private final ResourcePermissionRepository resourcePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public ResourcePermissionResponse grant(GrantPermissionRequest request) {
        validateGrantTarget(request);

        Permission permission = permissionRepository.findById(request.permissionId())
                .orElseThrow(() -> new NotFoundException("Permission", request.permissionId()));

        ResourcePermission resourcePermission;
        if (request.roleId() != null) {
            Role role = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new NotFoundException("Role", request.roleId()));
            resourcePermission = ResourcePermission.forRole(role, permission,
                    request.resourceId(), request.resourceType(),
                    request.grantedBy(), request.expiresAt());
        } else {
            resourcePermission = ResourcePermission.forUser(request.userId(), permission,
                    request.resourceId(), request.resourceType(),
                    request.grantedBy(), request.expiresAt());
        }

        return ResourcePermissionResponse.from(resourcePermissionRepository.save(resourcePermission));
    }

    @Override
    @Transactional(readOnly = true)
    public ResourcePermissionResponse getById(UUID id) {
        return resourcePermissionRepository.findById(id)
                .map(ResourcePermissionResponse::from)
                .orElseThrow(() -> new NotFoundException("ResourcePermission", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResourcePermissionResponse> list(Pageable pageable) {
        return resourcePermissionRepository.findAll(pageable).map(ResourcePermissionResponse::from);
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        ResourcePermission rp = resourcePermissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ResourcePermission", id));
        rp.revoke();
    }

    private void validateGrantTarget(GrantPermissionRequest request) {
        boolean hasUser = request.userId() != null;
        boolean hasRole = request.roleId() != null;
        if (hasUser == hasRole) {
            throw new IllegalArgumentException(
                    "Grant must target exactly one of userId or roleId, not both or neither");
        }
    }
}
