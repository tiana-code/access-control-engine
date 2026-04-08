package com.accesscontrol.service;

import com.accesscontrol.dto.request.StartWorkflowRequest;
import com.accesscontrol.dto.request.WorkflowTransitionRequest;
import com.accesscontrol.dto.response.WorkflowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WorkflowEngine {

    WorkflowResponse start(StartWorkflowRequest request);

    WorkflowResponse transition(UUID workflowId, WorkflowTransitionRequest request);

    WorkflowResponse getById(UUID id);

    Page<WorkflowResponse> list(Pageable pageable);
}
