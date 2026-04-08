package com.accesscontrol.service;

import com.accesscontrol.dto.request.CreatePermissionRequest;
import com.accesscontrol.dto.response.PermissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PermissionService {

    PermissionResponse create(CreatePermissionRequest request);

    PermissionResponse getById(UUID id);

    Page<PermissionResponse> list(Pageable pageable);

    void delete(UUID id);
}
