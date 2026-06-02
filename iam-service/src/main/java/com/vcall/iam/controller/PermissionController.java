package com.vcall.iam.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.iam.dto.PermissionRequest;
import com.vcall.iam.dto.PermissionResponse;
import com.vcall.iam.service.PermissionService;
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
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> getAllPermissions(Pageable pageable) {
        Page<PermissionResponse> response = permissionService.getAllPermissions(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(@PathVariable Long id) {
        PermissionResponse response = permissionService.getPermission(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(@PathVariable Long id,
                                                                             @Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> searchPermissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Specification<com.vcall.iam.entity.Permission> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("resource")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("action")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        Page<PermissionResponse> response = permissionService.searchPermissions(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    public void exportPermissionsCsv(@RequestParam(required = false) String keyword,
                                     HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<PermissionResponse> permissions;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<com.vcall.iam.entity.Permission> spec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("resource")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("action")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    );
            permissions = permissionService.searchPermissions(spec, pageable);
        } else {
            permissions = permissionService.getAllPermissions(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Resource", "Action", "Description");
        List<List<String>> rows = CsvExportUtil.toRows(permissions.getContent(),
                Arrays.asList("id", "name", "resource", "action", "description"));
        CsvExportUtil.writeCsv(response, "permissions.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportPermissionsExcel(@RequestParam(required = false) String keyword,
                                       HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<PermissionResponse> permissions;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<com.vcall.iam.entity.Permission> spec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("resource")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("action")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    );
            permissions = permissionService.searchPermissions(spec, pageable);
        } else {
            permissions = permissionService.getAllPermissions(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Resource", "Action", "Description");
        ExcelExportUtil.writeExcel(response, "permissions.xlsx", headers, permissions.getContent(),
                Arrays.asList("id", "name", "resource", "action", "description"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = permissionService.getPermissionStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
