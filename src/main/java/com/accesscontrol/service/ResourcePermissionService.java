package com.accesscontrol.service;

import com.accesscontrol.dto.request.GrantPermissionRequest;
import com.accesscontrol.dto.response.ResourcePermissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ResourcePermissionService {

    ResourcePermissionResponse grant(GrantPermissionRequest request);

    ResourcePermissionResponse getById(UUID id);

    Page<ResourcePermissionResponse> list(Pageable pageable);

    void revoke(UUID id);
}
