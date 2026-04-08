package com.accesscontrol.repository;

import com.accesscontrol.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByActionAndResourceType(String action, String resourceType);

    boolean existsByActionAndResourceType(String action, String resourceType);
}
