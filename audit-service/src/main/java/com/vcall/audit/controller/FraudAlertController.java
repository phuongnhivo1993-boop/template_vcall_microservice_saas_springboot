package com.vcall.audit.controller;

import com.vcall.audit.dto.FraudAlertRequest;
import com.vcall.audit.dto.FraudAlertResponse;
import com.vcall.audit.dto.FraudAlertStatusRequest;
import com.vcall.audit.entity.FraudAlert;
import com.vcall.audit.service.FraudDetectionService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/audit/fraud-alerts")
@RequiredArgsConstructor
public class FraudAlertController {

    private final FraudDetectionService fraudDetectionService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<FraudAlertResponse>>> getAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            Pageable pageable) {
        FraudAlert.AlertStatus alertStatus = status != null ? FraudAlert.AlertStatus.valueOf(status.toUpperCase()) : null;
        FraudAlert.Severity alertSeverity = severity != null ? FraudAlert.Severity.valueOf(severity.toUpperCase()) : null;
        Page<FraudAlertResponse> responses = fraudDetectionService.getAlerts(alertStatus, alertSeverity, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> getAlertById(@PathVariable UUID id) {
        FraudAlertResponse response = fraudDetectionService.getAlertById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> updateAlertStatus(
            @PathVariable UUID id,
            @Valid @RequestBody FraudAlertStatusRequest request) {
        FraudAlertResponse response = fraudDetectionService.updateAlertStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Alert status updated", response));
    }

    @PostMapping("/detect")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> detectFraud(
            @Valid @RequestBody FraudAlertRequest request) {
        FraudAlertResponse response = fraudDetectionService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fraud alert created", response));
    }
}
