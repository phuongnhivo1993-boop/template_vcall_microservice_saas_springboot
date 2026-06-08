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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllLogs(Pageable pageable) {
        AuditSearchRequest request = AuditSearchRequest.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();
        Page<AuditLogResponse> page = auditLogService.searchLogs(request);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getLogById(@PathVariable UUID id) {
        AuditLogResponse response = auditLogService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
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
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByActor(@PathVariable UUID actorId,
                                                                           Pageable pageable) {
        Page<AuditLogResponse> responses = auditLogService.getByActor(actorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByResource(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            Pageable pageable) {
        Page<AuditLogResponse> responses = auditLogService.getByResource(resourceType, resourceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLog(@PathVariable UUID id) {
        auditLogService.deleteLog(id);
        return ResponseEntity.ok(ApiResponse.success("Audit log deleted successfully", null));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLogResponse> logs = auditLogService.exportLogs(startDate, endDate);
        StringBuilder csv = new StringBuilder("Timestamp,Actor,Action,Resource,ResourceId,Status,Details\n");
        for (AuditLogResponse log : logs) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                    log.getTimestamp(), log.getActorId(), log.getAction(),
                    log.getResource(), log.getResourceId(), log.getStatus(),
                    log.getDetails() != null ? log.getDetails().replace(",", ";") : ""));
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "audit-logs.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> exportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLogResponse> logs = auditLogService.exportLogs(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        Map<String, Long> stats = auditLogService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanupLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
        auditLogService.cleanupLogs(before);
        return ResponseEntity.ok(ApiResponse.success("Audit logs cleaned up successfully", null));
    }
}
