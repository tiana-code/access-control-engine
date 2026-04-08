package com.accesscontrol.repository;

import com.accesscontrol.model.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, UUID> {

    @Query("SELECT w FROM ApprovalWorkflow w LEFT JOIN FETCH w.events WHERE w.id = :id")
    Optional<ApprovalWorkflow> findByIdWithEvents(@Param("id") UUID id);
}
