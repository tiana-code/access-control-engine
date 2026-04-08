package com.accesscontrol.service.impl;

import com.accesscontrol.dto.request.CreateRoleRequest;
import com.accesscontrol.dto.response.RoleResponse;
import com.accesscontrol.exception.ConflictException;
import com.accesscontrol.exception.NotFoundException;
import com.accesscontrol.model.Permission;
import com.accesscontrol.model.Role;
import com.accesscontrol.repository.PermissionRepository;
import com.accesscontrol.repository.RoleRepository;
import com.accesscontrol.service.RoleHierarchy;
import com.accesscontrol.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleHierarchy roleHierarchy;

    @Override
    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new ConflictException("Role already exists: " + request.name());
        }

        Role role = new Role(request.name(), request.description());

        if (request.parentRoleId() != null) {
            Role parent = roleRepository.findById(request.parentRoleId())
                    .orElseThrow(() -> new NotFoundException("Parent role", request.parentRoleId()));
            role.setParentRole(parent);
        }

        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(UUID id) {
        return roleRepository.findByIdWithPermissions(id)
                .map(RoleResponse::from)
                .orElseThrow(() -> new NotFoundException("Role", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> list(Pageable pageable) {
        return roleRepository.findAll(pageable).map(RoleResponse::from);
    }

    @Override
    @Transactional
    public RoleResponse assignPermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new NotFoundException("Role", roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NotFoundException("Permission", permissionId));

        role.addPermission(permission);
        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse revokePermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new NotFoundException("Role", roleId));

        role.getPermissions().stream()
                .filter(p -> p.getId().equals(permissionId))
                .findFirst()
                .ifPresent(role::removePermission);
        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permission> getEffectivePermissions(UUID roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new NotFoundException("Role", roleId);
        }
        return roleHierarchy.resolveEffectivePermissions(roleId);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new NotFoundException("Role", id);
        }
        roleRepository.deleteById(id);
    }

}
