package com.accesscontrol.model;

import com.accesscontrol.model.enums.WorkflowAction;
import com.accesscontrol.model.enums.WorkflowState;
import com.accesscontrol.service.WorkflowTransitions;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "approval_workflows")
@Getter
public class ApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @NotNull
    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @NotBlank
    @Size(max = 128)
    @Column(name = "resource_type", nullable = false, length = 128)
    private String resourceType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WorkflowState state = WorkflowState.DRAFT;

    @Column(name = "initiator_id", nullable = false)
    private UUID initiatorId;

    @Setter
    @Column(name = "current_assignee_id")
    private UUID currentAssigneeId;

    @Setter
    @Size(max = 2048)
    @Column(length = 2048)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("occurredAt ASC")
    private List<WorkflowEvent> events = new ArrayList<>();

    protected ApprovalWorkflow() {
    }

    public ApprovalWorkflow(UUID resourceId, String resourceType, UUID initiatorId) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.initiatorId = initiatorId;
    }

    public List<WorkflowEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addEvent(WorkflowEvent event) {
        events.add(event);
        event.setWorkflow(this);
    }

    public void transition(WorkflowAction action, UUID actorId, String comment) {
        if (this.state.isTerminal()) {
            throw new IllegalStateException("Cannot transition from terminal state: " + this.state);
        }

        WorkflowState targetState = WorkflowTransitions.resolve(this.state, action)
                .orElseThrow(() -> new IllegalStateException(
                        "Invalid transition: action " + action + " not allowed from state " + this.state));

        WorkflowState previousState = this.state;
        this.state = targetState;
        this.updatedAt = Instant.now();

        if (targetState.isTerminal()) {
            this.completedAt = Instant.now();
        }

        WorkflowEvent event = new WorkflowEvent(this, action, previousState, targetState, actorId, comment);
        addEvent(event);
    }
}
