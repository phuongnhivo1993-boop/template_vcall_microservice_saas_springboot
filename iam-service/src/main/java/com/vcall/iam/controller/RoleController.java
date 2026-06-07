package com.vcall.iam.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.iam.dto.RoleRequest;
import com.vcall.iam.dto.RoleResponse;
import com.vcall.iam.service.RoleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> getAllRoles(Pageable pageable) {
        Page<RoleResponse> response = roleService.getAllRoles(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable Long id,
                                                                @Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    @PostMapping("/{roleId}/users/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(@PathVariable Long roleId,
                                                              @PathVariable UUID userId) {
        roleService.assignRoleToUser(roleId, userId);
        return ResponseEntity.ok(ApiResponse.success("Role assigned to user successfully", null));
    }

    @DeleteMapping("/{roleId}/users/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(@PathVariable Long roleId,
                                                                 @PathVariable UUID userId) {
        roleService.removeRoleFromUser(roleId, userId);
        return ResponseEntity.ok(ApiResponse.success("Role removed from user successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> searchRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Specification<com.vcall.iam.entity.Role> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        Page<RoleResponse> response = roleService.searchRoles(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void exportRolesCsv(@RequestParam(required = false) String keyword,
                               HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<RoleResponse> roles;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<com.vcall.iam.entity.Role> spec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    );
            roles = roleService.searchRoles(spec, pageable);
        } else {
            roles = roleService.getAllRoles(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description");
        List<List<String>> rows = CsvExportUtil.toRows(roles.getContent(),
                Arrays.asList("id", "name", "description"));
        CsvExportUtil.writeCsv(response, "roles.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void exportRolesExcel(@RequestParam(required = false) String keyword,
                                 HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<RoleResponse> roles;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<com.vcall.iam.entity.Role> spec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    );
            roles = roleService.searchRoles(spec, pageable);
        } else {
            roles = roleService.getAllRoles(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description");
        ExcelExportUtil.writeExcel(response, "roles.xlsx", headers, roles.getContent(),
                Arrays.asList("id", "name", "description"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = roleService.getRoleStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
