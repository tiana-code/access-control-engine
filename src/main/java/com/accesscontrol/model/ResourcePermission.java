package com.accesscontrol.model;

import com.accesscontrol.model.enums.GrantState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resource_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id", "permission_id", "resource_id"}))
@Getter
public class ResourcePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Size(max = 128)
    @Column(name = "resource_type", length = 128)
    private String resourceType;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();

    @Column(name = "granted_by")
    private UUID grantedBy;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_state", nullable = false, length = 16)
    private GrantState grantState = GrantState.ACTIVE;

    protected ResourcePermission() {
    }

    private ResourcePermission(UUID userId, Role role, Permission permission,
                               UUID resourceId, String resourceType,
                               UUID grantedBy, Instant expiresAt) {
        this.userId = userId;
        this.role = role;
        this.permission = permission;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.grantedBy = grantedBy;
        this.expiresAt = expiresAt;
    }

    public static ResourcePermission forUser(UUID userId, Permission permission,
                                             UUID resourceId, String resourceType,
                                             UUID grantedBy, Instant expiresAt) {
        if (userId == null) throw new IllegalArgumentException("userId must not be null for user grant");
        if (permission == null) throw new IllegalArgumentException("permission must not be null");
        return new ResourcePermission(userId, null, permission, resourceId, resourceType, grantedBy, expiresAt);
    }

    public static ResourcePermission forRole(Role role, Permission permission,
                                             UUID resourceId, String resourceType,
                                             UUID grantedBy, Instant expiresAt) {
        if (role == null) throw new IllegalArgumentException("role must not be null for role grant");
        if (permission == null) throw new IllegalArgumentException("permission must not be null");
        return new ResourcePermission(null, role, permission, resourceId, resourceType, grantedBy, expiresAt);
    }

    public boolean isExpired() {
        return grantState == GrantState.REVOKED
                || grantState == GrantState.EXPIRED
                || revoked
                || (expiresAt != null && Instant.now().isAfter(expiresAt));
    }

    public void revoke() {
        this.revoked = true;
        this.grantState = GrantState.REVOKED;
        this.expiresAt = Instant.now();
    }

    public void markExpired() {
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            this.grantState = GrantState.EXPIRED;
        }
    }

    public void extend(Instant newExpiresAt) {
        if (newExpiresAt != null && newExpiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("New expiration must be in the future");
        }
        this.expiresAt = newExpiresAt;
    }
}
