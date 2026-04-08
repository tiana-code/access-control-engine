package com.accesscontrol.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionTest {

    @Test
    void normalizesActionAndResourceTypeToUpperCase() {
        Permission perm = new Permission("  read  ", "  document  ");
        assertThat(perm.getAction()).isEqualTo("READ");
        assertThat(perm.getResourceType()).isEqualTo("DOCUMENT");
    }

    @Test
    void rejectsBlankAction() {
        assertThatThrownBy(() -> new Permission("", "Document"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("action");
    }

    @Test
    void rejectsBlankResourceType() {
        assertThatThrownBy(() -> new Permission("READ", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("resourceType");
    }

    @Test
    void rejectsNullAction() {
        assertThatThrownBy(() -> new Permission(null, "Document"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsWildcardAction() {
        Permission permission = new Permission("*", "Document");
        assertThat(permission.getAction()).isEqualTo("*");
    }

    @Test
    void acceptsWildcardResourceType() {
        Permission permission = new Permission("READ", "*");
        assertThat(permission.getResourceType()).isEqualTo("*");
    }

    @Test
    void storesDescription() {
        Permission permission = new Permission("WRITE", "Document", "Allow writing documents");
        assertThat(permission.getDescription()).isEqualTo("Allow writing documents");
    }
}
