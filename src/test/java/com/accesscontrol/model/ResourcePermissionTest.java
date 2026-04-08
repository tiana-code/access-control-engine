package com.accesscontrol.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourcePermissionTest {

    @Test
    void expiredGrantIsDetected() {
        Permission permission = new Permission("READ", "Document");
        ResourcePermission resourcePermission = ResourcePermission.forUser(
                UUID.randomUUID(), permission, UUID.randomUUID(), "Document",
                UUID.randomUUID(), Instant.now().minusSeconds(60));
        assertThat(resourcePermission.isExpired()).isTrue();
    }

    @Test
    void activeGrantIsNotExpired() {
        Permission permission = new Permission("READ", "Document");
        ResourcePermission resourcePermission = ResourcePermission.forUser(
                UUID.randomUUID(), permission, UUID.randomUUID(), "Document",
                UUID.randomUUID(), Instant.now().plusSeconds(3600));
        assertThat(resourcePermission.isExpired()).isFalse();
    }

    @Test
    void grantWithNoExpiryNeverExpires() {
        Permission permission = new Permission("READ", "Document");
        ResourcePermission resourcePermission = ResourcePermission.forUser(
                UUID.randomUUID(), permission, UUID.randomUUID(), "Document",
                UUID.randomUUID(), null);
        assertThat(resourcePermission.isExpired()).isFalse();
    }

    @Test
    void revokeMarksGrantAsExpired() {
        Permission permission = new Permission("WRITE", "Document");
        ResourcePermission resourcePermission = ResourcePermission.forUser(
                UUID.randomUUID(), permission, UUID.randomUUID(), "Document",
                UUID.randomUUID(), Instant.now().plusSeconds(3600));
        resourcePermission.revoke();
        assertThat(resourcePermission.isExpired()).isTrue();
    }

    @Test
    void extendRejectsDateInPast() {
        Permission permission = new Permission("READ", "Document");
        ResourcePermission resourcePermission = ResourcePermission.forUser(
                UUID.randomUUID(), permission, UUID.randomUUID(), "Document",
                UUID.randomUUID(), Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> resourcePermission.extend(Instant.now().minusSeconds(60)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void forUserRejectsNullUserId() {
        Permission permission = new Permission("READ", "Document");
        assertThatThrownBy(() -> ResourcePermission.forUser(
                null, permission, UUID.randomUUID(), "Document", UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void forRoleRejectsNullRole() {
        Permission permission = new Permission("READ", "Document");
        assertThatThrownBy(() -> ResourcePermission.forRole(
                null, permission, UUID.randomUUID(), "Document", UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void forUserRejectsNullPermission() {
        assertThatThrownBy(() -> ResourcePermission.forUser(
                UUID.randomUUID(), null, UUID.randomUUID(), "Document", UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
