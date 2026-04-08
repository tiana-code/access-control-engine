package com.accesscontrol.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"action", "resource_type"}))
@Getter
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, length = 64)
    private String action;

    @NotBlank
    @Size(max = 128)
    @Column(name = "resource_type", nullable = false, length = 128)
    private String resourceType;

    @Size(max = 512)
    @Column(length = 512)
    private String description;

    protected Permission() {
    }

    public Permission(String action, String resourceType) {
        this(action, resourceType, null);
    }

    public Permission(String action, String resourceType, String description) {
        if (action == null || action.isBlank())
            throw new IllegalArgumentException("action must not be blank");
        if (resourceType == null || resourceType.isBlank())
            throw new IllegalArgumentException("resourceType must not be blank");
        this.action = action.strip().toUpperCase(Locale.ROOT);
        this.resourceType = resourceType.strip().toUpperCase(Locale.ROOT);
        this.description = description;
    }
}
