package com.accesscontrol.repository;

import com.accesscontrol.model.ResourcePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ResourcePermissionRepository extends JpaRepository<ResourcePermission, UUID> {

    @Query("""
            SELECT rp FROM ResourcePermission rp
            JOIN FETCH rp.permission
            LEFT JOIN FETCH rp.role
            WHERE (rp.userId = :userId OR rp.role.id IN :roleIds)
              AND (rp.resourceId = :resourceId OR rp.resourceId IS NULL)
              AND (rp.resourceType = :resourceType OR rp.resourceType IS NULL)
              AND (rp.expiresAt IS NULL OR rp.expiresAt > CURRENT_TIMESTAMP)
            """)
    List<ResourcePermission> findActiveGrants(
            @Param("userId") UUID userId,
            @Param("roleIds") Set<UUID> roleIds,
            @Param("resourceId") UUID resourceId,
            @Param("resourceType") String resourceType);

    @Query("SELECT DISTINCT rp.role.id " +
            "FROM ResourcePermission rp " +
            "WHERE rp.userId = :userId " +
            "AND rp.role IS NOT NULL " +
            "AND (rp.expiresAt IS NULL OR rp.expiresAt > CURRENT_TIMESTAMP)")
    Set<UUID> findRoleIdsForUser(@Param("userId") UUID userId);

    List<ResourcePermission> findByUserId(UUID userId);

    List<ResourcePermission> findByRoleId(UUID roleId);
}
