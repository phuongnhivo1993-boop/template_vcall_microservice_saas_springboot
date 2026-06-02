package com.vcall.iam.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.iam.dto.PermissionRequest;
import com.vcall.iam.dto.PermissionResponse;
import com.vcall.iam.entity.Permission;
import com.vcall.iam.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Permission already exists: " + request.getName());
        }
        Permission permission = Permission.builder()
                .name(request.getName())
                .resource(request.getResource())
                .action(request.getAction())
                .description(request.getDescription())
                .build();
        permission = permissionRepository.save(permission);
        return toResponse(permission);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        return toResponse(permission);
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        permission.setName(request.getName());
        permission.setResource(request.getResource());
        permission.setAction(request.getAction());
        permission.setDescription(request.getDescription());
        permission = permissionRepository.save(permission);
        return toResponse(permission);
    }

    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        permission.setIsDeleted(true);
        permissionRepository.save(permission);
    }

    private PermissionResponse toResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .resource(permission.getResource())
                .action(permission.getAction())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}
