package com.accesscontrol.service.impl;

import com.accesscontrol.dto.request.CreatePermissionRequest;
import com.accesscontrol.dto.response.PermissionResponse;
import com.accesscontrol.exception.ConflictException;
import com.accesscontrol.exception.NotFoundException;
import com.accesscontrol.model.Permission;
import com.accesscontrol.repository.PermissionRepository;
import com.accesscontrol.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.existsByActionAndResourceType(request.action(), request.resourceType())) {
            throw new ConflictException("Permission already exists: " + request.action() + " on " + request.resourceType());
        }

        Permission permission = new Permission(
                request.action(), request.resourceType(), request.description());

        return PermissionResponse.from(permissionRepository.save(permission));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getById(UUID id) {
        return permissionRepository.findById(id)
                .map(PermissionResponse::from)
                .orElseThrow(() -> new NotFoundException("Permission", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionResponse> list(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(PermissionResponse::from);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!permissionRepository.existsById(id)) {
            throw new NotFoundException("Permission", id);
        }
        permissionRepository.deleteById(id);
    }
}
