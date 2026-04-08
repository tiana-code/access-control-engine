package com.accesscontrol.service;

import com.accesscontrol.exception.ConflictException;
import com.accesscontrol.model.Permission;
import com.accesscontrol.model.Role;
import com.accesscontrol.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleHierarchy {

    private final RoleRepository roleRepository;

    public Set<Role> resolveEffectiveRoles(UUID roleId) {
        Set<Role> effective = new HashSet<>();
        Deque<UUID> queue = new ArrayDeque<>();
        queue.push(roleId);

        while (!queue.isEmpty()) {
            UUID current = queue.pop();
            roleRepository.findById(current).ifPresent(role -> {
                if (effective.add(role) && role.getParentRole() != null) {
                    queue.push(role.getParentRole().getId());
                }
            });
        }
        return effective;
    }

    public Set<Permission> resolveEffectivePermissions(UUID roleId) {
        Set<Permission> permissions = new HashSet<>();
        for (Role role : resolveEffectiveRoles(roleId)) {
            permissions.addAll(role.getPermissions());
        }
        return permissions;
    }

    public boolean isDescendantOf(UUID candidateRoleId, UUID ancestorRoleId) {
        return resolveEffectiveRoles(candidateRoleId)
                .stream()
                .anyMatch(r -> r.getId().equals(ancestorRoleId));
    }

    public void validateNoCycle(UUID roleId, UUID newParentId) {
        if (roleId.equals(newParentId)) {
            throw new ConflictException("Role cannot be its own parent");
        }
        if (isDescendantOf(newParentId, roleId)) {
            throw new ConflictException("Assigning this parent would create a cycle in the role hierarchy");
        }
    }
}
