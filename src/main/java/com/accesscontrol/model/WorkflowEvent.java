package com.accesscontrol.model;

import com.accesscontrol.model.enums.WorkflowAction;
import com.accesscontrol.model.enums.WorkflowState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_events")
@Getter
public class WorkflowEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private ApprovalWorkflow workflow;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 32)
    private WorkflowAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", length = 32)
    private WorkflowState fromState;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false, length = 32)
    private WorkflowState toState;

    @Column(name = "actor_id")
    private UUID actorId;

    @Size(max = 2048)
    @Column(length = 2048)
    private String comment;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();

    protected WorkflowEvent() {
    }

    WorkflowEvent(ApprovalWorkflow workflow, WorkflowAction action,
                  WorkflowState fromState, WorkflowState toState,
                  UUID actorId, String comment) {
        this.workflow = workflow;
        this.action = action;
        this.fromState = fromState;
        this.toState = toState;
        this.actorId = actorId;
        this.comment = comment;
    }

    void setWorkflow(ApprovalWorkflow workflow) {
        this.workflow = workflow;
    }
}
