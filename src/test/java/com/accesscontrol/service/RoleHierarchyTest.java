package com.accesscontrol.service;

import com.accesscontrol.model.Role;
import com.accesscontrol.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleHierarchyTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleHierarchy roleHierarchy;

    @Test
    void resolveEffectiveRoles_returnsSelfWhenNoParent() {
        UUID roleId = UUID.randomUUID();
        Role role = roleWithId(roleId, "EDITOR", null);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        Set<Role> result = roleHierarchy.resolveEffectiveRoles(roleId);

        assertThat(result).containsExactly(role);
    }

    @Test
    void resolveEffectiveRoles_includesFullParentChain() {
        UUID grandparentId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Role grandparent = roleWithId(grandparentId, "ADMIN", null);
        Role parent = roleWithId(parentId, "MANAGER", grandparent);
        Role child = roleWithId(childId, "EDITOR", parent);

        when(roleRepository.findById(childId)).thenReturn(Optional.of(child));
        when(roleRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(roleRepository.findById(grandparentId)).thenReturn(Optional.of(grandparent));

        Set<Role> result = roleHierarchy.resolveEffectiveRoles(childId);

        assertThat(result).containsExactlyInAnyOrder(child, parent, grandparent);
    }

    @Test
    void isDescendantOf_returnsTrueForDirectParent() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Role parent = roleWithId(parentId, "ADMIN", null);
        Role child = roleWithId(childId, "EDITOR", parent);

        when(roleRepository.findById(childId)).thenReturn(Optional.of(child));
        when(roleRepository.findById(parentId)).thenReturn(Optional.of(parent));

        assertThat(roleHierarchy.isDescendantOf(childId, parentId)).isTrue();
    }

    @Test
    void isDescendantOf_returnsFalseForUnrelatedRole() {
        UUID roleAId = UUID.randomUUID();
        UUID roleBId = UUID.randomUUID();

        Role roleA = roleWithId(roleAId, "AUDITOR", null);
        when(roleRepository.findById(roleAId)).thenReturn(Optional.of(roleA));

        assertThat(roleHierarchy.isDescendantOf(roleAId, roleBId)).isFalse();
    }

    private Role roleWithId(UUID id, String name, Role parent) {
        Role role = new Role(name);
        try {
            Field idField = Role.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(role, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        role.setParentRole(parent);
        return role;
    }
}
