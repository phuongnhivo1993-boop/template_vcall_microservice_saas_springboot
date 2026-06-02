package com.vcall.audit.controller;

import com.vcall.audit.dto.AuditLogResponse;
import com.vcall.audit.dto.AuditSearchRequest;
import com.vcall.audit.service.AuditLogService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllLogs(Pageable pageable) {
        AuditSearchRequest request = AuditSearchRequest.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();
        Page<AuditLogResponse> page = auditLogService.searchLogs(request);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getLogById(@PathVariable UUID id) {
        AuditLogResponse response = auditLogService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> searchLogs(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AuditSearchRequest request = AuditSearchRequest.builder()
                .actorId(actorId)
                .action(action)
                .resource(resource)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .page(page)
                .size(size)
                .build();

        Page<AuditLogResponse> result = auditLogService.searchLogs(request);
        PagedResponse<AuditLogResponse> paged = PagedResponse.<AuditLogResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByActor(@PathVariable UUID actorId,
                                                                           Pageable pageable) {
        Page<AuditLogResponse> responses = auditLogService.getByActor(actorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByResource(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            Pageable pageable) {
        Page<AuditLogResponse> responses = auditLogService.getByResource(resourceType, resourceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
