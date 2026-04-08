package com.accesscontrol.service.impl;

import com.accesscontrol.dto.request.StartWorkflowRequest;
import com.accesscontrol.dto.request.WorkflowTransitionRequest;
import com.accesscontrol.dto.response.WorkflowResponse;
import com.accesscontrol.exception.InvalidTransitionException;
import com.accesscontrol.exception.NotFoundException;
import com.accesscontrol.model.ApprovalWorkflow;
import com.accesscontrol.model.enums.WorkflowState;
import com.accesscontrol.repository.ApprovalWorkflowRepository;
import com.accesscontrol.service.WorkflowEngine;
import com.accesscontrol.service.WorkflowTransitions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowEngineImpl implements WorkflowEngine {

    private final ApprovalWorkflowRepository workflowRepository;

    @Override
    @Transactional
    public WorkflowResponse start(StartWorkflowRequest request) {
        ApprovalWorkflow workflow = new ApprovalWorkflow(
                request.resourceId(), request.resourceType(), request.initiatorId());
        workflow.setDescription(request.description());
        workflow.transition(WorkflowState.PENDING_REVIEW, request.initiatorId(), "Workflow started");

        return WorkflowResponse.from(workflowRepository.save(workflow),
                WorkflowTransitions.allowedActions(WorkflowState.PENDING_REVIEW));
    }

    @Override
    @Transactional
    public WorkflowResponse transition(UUID workflowId, WorkflowTransitionRequest request) {
        ApprovalWorkflow workflow = workflowRepository.findByIdWithEvents(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow", workflowId));

        if (workflow.getState().isTerminal()) {
            throw new InvalidTransitionException(
                    "Workflow is in terminal state: " + workflow.getState());
        }

        WorkflowState currentState = workflow.getState();
        WorkflowState nextState = WorkflowTransitions.resolve(currentState, request.action())
                .orElseThrow(() -> new InvalidTransitionException(
                        "Action " + request.action() + " is not allowed from state " + currentState +
                                ". Allowed: " + WorkflowTransitions.allowedActions(currentState)));

        workflow.transition(nextState, request.actorId(), request.comment());

        if (request.assigneeId() != null) {
            workflow.setCurrentAssigneeId(request.assigneeId());
        }

        return WorkflowResponse.from(workflowRepository.save(workflow), WorkflowTransitions.allowedActions(nextState));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowResponse getById(UUID id) {
        return workflowRepository.findByIdWithEvents(id)
                .map(wf
                        -> WorkflowResponse.from(wf, WorkflowTransitions.allowedActions(wf.getState())))
                .orElseThrow(() -> new NotFoundException("Workflow", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkflowResponse> list(Pageable pageable) {
        return workflowRepository.findAll(pageable)
                .map(wf -> WorkflowResponse.from(wf, WorkflowTransitions.allowedActions(wf.getState())));
    }
}
