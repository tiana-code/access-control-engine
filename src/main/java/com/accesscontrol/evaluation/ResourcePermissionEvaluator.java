package com.accesscontrol.evaluation;

import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.model.Permission;
import com.accesscontrol.model.ResourcePermission;
import com.accesscontrol.repository.ResourcePermissionRepository;
import com.accesscontrol.service.RoleHierarchy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResourcePermissionEvaluator implements PermissionEvaluator {

    private final ResourcePermissionRepository resourcePermissionRepository;
    private final RoleHierarchy roleHierarchy;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (targetDomainObject == null) {
            return false;
        }
        String action = permission.toString().toUpperCase(Locale.ROOT);
        return evaluate(resolveUserId(authentication), null,
                targetDomainObject.getClass().getSimpleName(), action);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        UUID resourceId = targetId instanceof UUID uid ? uid : UUID.fromString(targetId.toString());
        String action = permission.toString().toUpperCase(Locale.ROOT);
        return evaluate(resolveUserId(authentication), resourceId, targetType, action);
    }

    boolean evaluate(UUID userId, UUID resourceId, String resourceType, String action) {
        Set<UUID> directRoleIds = resourcePermissionRepository.findRoleIdsForUser(userId);
        Set<UUID> effectiveRoleIds = expandRoleHierarchy(directRoleIds);

        List<ResourcePermission> grants = resourcePermissionRepository.findActiveGrants(
                userId, effectiveRoleIds.isEmpty()
                        ? Collections.singleton(new UUID(0, 0)) : effectiveRoleIds,
                resourceId, resourceType);

        return grants.stream()
                .filter(g -> !g.isExpired())
                .anyMatch(g -> matchesAction(g.getPermission(), action, resourceType));
    }

    private boolean matchesAction(Permission permissionValue, String action, String resourceType) {
        boolean actionMatches = permissionValue.getAction().equalsIgnoreCase(action)
                || "*".equals(permissionValue.getAction());
        boolean typeMatches = permissionValue.getResourceType().equalsIgnoreCase(resourceType)
                || "*".equals(permissionValue.getResourceType());
        return actionMatches && typeMatches;
    }

    private Set<UUID> expandRoleHierarchy(Set<UUID> roleIds) {
        if (roleIds.isEmpty()) return Collections.emptySet();
        Set<UUID> expanded = new HashSet<>(roleIds);
        for (UUID roleId : roleIds) {
            roleHierarchy.resolveEffectiveRoles(roleId).forEach(r -> expanded.add(r.getId()));
        }
        return expanded;
    }

    private UUID resolveUserId(Authentication authentication) {
        String name = authentication.getName();
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            throw new AccessControlException(
                "Cannot resolve user identity: authentication name '" + name +
                "' is not a valid UUID. Configure your security to provide UUID-based principal names.");
        }
    }
}
