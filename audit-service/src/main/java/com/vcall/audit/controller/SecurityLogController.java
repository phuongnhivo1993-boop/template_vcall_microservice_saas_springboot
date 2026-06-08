package com.vcall.audit.controller;

import com.vcall.audit.dto.SecurityLogResponse;
import com.vcall.audit.entity.SecurityLog;
import com.vcall.audit.service.SecurityLogService;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/audit/security-logs")
@RequiredArgsConstructor
public class SecurityLogController {

    private final SecurityLogService securityLogService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SecurityLogResponse>>> getAllLogs(Pageable pageable) {
        Page<SecurityLogResponse> responses = securityLogService.getAllLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SecurityLogResponse>> getLogById(@PathVariable UUID id) {
        SecurityLogResponse response = securityLogService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/events/{eventType}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SecurityLogResponse>>> getByEventType(
            @PathVariable String eventType, Pageable pageable) {
        SecurityLog.EventType type = SecurityLog.EventType.valueOf(eventType.toUpperCase());
        Page<SecurityLogResponse> responses = securityLogService.getByEventType(type, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/suspicious")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SecurityLogResponse>>> getSuspicious() {
        List<SecurityLogResponse> responses = securityLogService.detectSuspiciousActivity();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/login-history/{actorId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<SecurityLogResponse>>> getLoginHistory(
            @PathVariable UUID actorId) {
        List<SecurityLogResponse> responses = securityLogService.getLoginHistory(actorId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
