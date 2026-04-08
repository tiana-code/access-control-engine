package com.accesscontrol.evaluation;

import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.model.Permission;
import com.accesscontrol.model.ResourcePermission;
import com.accesscontrol.model.Role;
import com.accesscontrol.repository.ResourcePermissionRepository;
import com.accesscontrol.service.RoleHierarchy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourcePermissionEvaluatorTest {

    @Mock
    private ResourcePermissionRepository resourcePermissionRepository;

    @Mock
    private RoleHierarchy roleHierarchy;

    @InjectMocks
    private ResourcePermissionEvaluator evaluator;

    @Test
    void hasPermission_returnsFalseForNullAuthentication() {
        boolean result = evaluator.hasPermission(null, UUID.randomUUID(), "Document", "READ");
        assertThat(result).isFalse();
    }

    @Test
    void hasPermission_throwsForNonUuidPrincipalName() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("not-a-uuid");

        assertThatThrownBy(() ->
                evaluator.hasPermission(auth, UUID.randomUUID(), "Document", "READ")
        ).isInstanceOf(AccessControlException.class)
                .hasMessageContaining("not a valid UUID");
    }

    @Test
    void evaluate_returnsTrueWhenUserHasMatchingActiveGrant() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String resourceType = "Document";
        String action = "READ";

        Permission permission = new Permission(action, resourceType);
        ResourcePermission grant = ResourcePermission.forUser(userId, permission, resourceId, resourceType, null, null);

        when(resourcePermissionRepository.findRoleIdsForUser(userId)).thenReturn(Collections.emptySet());
        when(resourcePermissionRepository.findActiveGrants(eq(userId), any(), eq(resourceId), eq(resourceType)))
                .thenReturn(List.of(grant));

        boolean result = evaluator.evaluate(userId, resourceId, resourceType, action);

        assertThat(result).isTrue();
    }

    @Test
    void evaluate_returnsFalseWhenNoMatchingGrantExists() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        when(resourcePermissionRepository.findRoleIdsForUser(userId)).thenReturn(Collections.emptySet());
        when(resourcePermissionRepository.findActiveGrants(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        boolean result = evaluator.evaluate(userId, resourceId, "Document", "WRITE");

        assertThat(result).isFalse();
    }

    @Test
    void evaluate_wildcardActionMatchesAnyPermissionAction() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String resourceType = "Document";

        Permission wildcardPermission = new Permission("*", resourceType);
        ResourcePermission grant = ResourcePermission.forUser(userId, wildcardPermission, resourceId, resourceType, null, null);

        when(resourcePermissionRepository.findRoleIdsForUser(userId)).thenReturn(Collections.emptySet());
        when(resourcePermissionRepository.findActiveGrants(eq(userId), any(), eq(resourceId), eq(resourceType)))
                .thenReturn(List.of(grant));

        assertThat(evaluator.evaluate(userId, resourceId, resourceType, "DELETE")).isTrue();
        assertThat(evaluator.evaluate(userId, resourceId, resourceType, "WRITE")).isTrue();
        assertThat(evaluator.evaluate(userId, resourceId, resourceType, "PUBLISH")).isTrue();
    }

    private Role roleWithId(UUID id) {
        Role role = new Role("TEST_ROLE");
        try {
            Field idField = Role.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(role, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return role;
    }
}
