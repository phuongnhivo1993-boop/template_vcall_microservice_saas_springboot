package com.vcall.audit.controller;

import com.vcall.audit.dto.ReconciliationAuditResponse;
import com.vcall.audit.entity.ReconciliationAudit;
import com.vcall.audit.service.ReconciliationAuditService;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<ApiResponse<List<ReconciliationAuditResponse>>> getReconciliations(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        ReconciliationAudit.ReconciliationType reconciliationType =
                type != null ? ReconciliationAudit.ReconciliationType.valueOf(type.toUpperCase()) : null;
        ReconciliationAudit.ReconciliationStatus reconciliationStatus =
                status != null ? ReconciliationAudit.ReconciliationStatus.valueOf(status.toUpperCase()) : null;
        List<ReconciliationAuditResponse> responses =
                reconciliationAuditService.getDiscrepancies(reconciliationType, reconciliationStatus);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
