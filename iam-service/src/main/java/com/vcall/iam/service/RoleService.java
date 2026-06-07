package com.vcall.iam.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.iam.dto.RoleRequest;
import com.vcall.iam.dto.RoleResponse;
import com.vcall.iam.entity.Permission;
import com.vcall.iam.entity.Role;
import com.vcall.iam.entity.RoleName;
import com.vcall.iam.entity.RolePermission;
import com.vcall.iam.entity.User;
import com.vcall.iam.entity.UserRole;
import com.vcall.iam.repository.PermissionRepository;
import com.vcall.iam.repository.RolePermissionRepository;
import com.vcall.iam.repository.RoleRepository;
import com.vcall.iam.repository.UserRepository;
import com.vcall.iam.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse createRole(RoleRequest request) {
        RoleName roleName = RoleName.valueOf(request.getName().toUpperCase());
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new DuplicateResourceException("Role already exists: " + request.getName());
        }

        Role role = Role.builder()
                .name(roleName)
                .description(request.getDescription())
                .build();

        role = roleRepository.save(role);

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<RolePermission> rolePermissions = new HashSet<>();
            for (Long permId : request.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permId));
                RolePermission rp = RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .build();
                rolePermissions.add(rolePermissionRepository.save(rp));
            }
            role.setRolePermissions(rolePermissions);
        }

        return toRoleResponse(role);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#id")
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return toRoleResponse(role);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(this::toRoleResponse);
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> searchRoles(Specification<Role> spec, Pageable pageable) {
        return roleRepository.findAll(spec, pageable)
                .map(this::toRoleResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRoleStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRoles", roleRepository.count());
        return stats;
    }

    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        role.setDescription(request.getDescription());
        role = roleRepository.save(role);

        if (request.getPermissionIds() != null) {
            rolePermissionRepository.deleteByRoleId(id);
            Set<RolePermission> rolePermissions = new HashSet<>();
            for (Long permId : request.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permId));
                RolePermission rp = RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .build();
                rolePermissions.add(rolePermissionRepository.save(rp));
            }
            role.setRolePermissions(rolePermissions);
        }

        return toRoleResponse(role);
    }

    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        role.setIsDeleted(true);
        roleRepository.save(role);
    }

    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public void assignRoleToUser(Long roleId, UUID userId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (userRoleRepository.findByUserIdAndRoleId(userId, roleId).isPresent()) {
            throw new DuplicateResourceException("User already has this role");
        }

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();
        userRoleRepository.save(userRole);
    }

    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public void removeRoleFromUser(Long roleId, UUID userId) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new ResourceNotFoundException("User role association not found"));
        userRoleRepository.delete(userRole);
    }

    private RoleResponse toRoleResponse(Role role) {
        Set<String> permissions = role.getRolePermissions().stream()
                .map(rp -> rp.getPermission().getName())
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName().name())
                .description(role.getDescription())
                .permissions(permissions)
                .build();
    }
}
