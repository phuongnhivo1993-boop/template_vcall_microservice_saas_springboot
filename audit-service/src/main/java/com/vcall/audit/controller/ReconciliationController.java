package com.vcall.audit.controller;

import com.vcall.audit.dto.ReconciliationAuditResponse;
import com.vcall.audit.entity.ReconciliationAudit;
import com.vcall.audit.service.ReconciliationAuditService;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationAuditService reconciliationAuditService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<ReconciliationAuditResponse>> runReconciliation(
            @RequestParam String type) {
        ReconciliationAudit.ReconciliationType reconciliationType =
                ReconciliationAudit.ReconciliationType.valueOf(type.toUpperCase());
        ReconciliationAuditResponse response =
                reconciliationAuditService.runReconciliation(reconciliationType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reconciliation completed", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReconciliationAuditResponse>> getReconciliationById(@PathVariable UUID id) {
        ReconciliationAuditResponse response = reconciliationAuditService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReconciliationAuditResponse>>> getReconciliations(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        ReconciliationAudit.ReconciliationType reconciliationType =
                type != null ? ReconciliationAudit.ReconciliationType.valueOf(type.toUpperCase()) : null;
        ReconciliationAudit.ReconciliationStatus reconciliationStatus =
                status != null ? ReconciliationAudit.ReconciliationStatus.valueOf(status.toUpperCase()) : null;
        Page<ReconciliationAuditResponse> responses =
                reconciliationAuditService.getDiscrepancies(reconciliationType, reconciliationStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
