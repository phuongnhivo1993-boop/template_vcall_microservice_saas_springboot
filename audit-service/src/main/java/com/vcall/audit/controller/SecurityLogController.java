package com.vcall.audit.controller;

import com.vcall.audit.dto.SecurityLogResponse;
import com.vcall.audit.entity.SecurityLog;
import com.vcall.audit.service.SecurityLogService;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/security-logs")
@RequiredArgsConstructor
public class SecurityLogController {

    private final SecurityLogService securityLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SecurityLogResponse>>> getAllLogs() {
        List<SecurityLogResponse> responses = securityLogService.getAllLogs();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SecurityLogResponse>> getLogById(@PathVariable UUID id) {
        SecurityLogResponse response = securityLogService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/events/{eventType}")
    public ResponseEntity<ApiResponse<List<SecurityLogResponse>>> getByEventType(
            @PathVariable String eventType) {
        SecurityLog.EventType type = SecurityLog.EventType.valueOf(eventType.toUpperCase());
        List<SecurityLogResponse> responses = securityLogService.getByEventType(type);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/suspicious")
    public ResponseEntity<ApiResponse<List<SecurityLogResponse>>> getSuspicious() {
        List<SecurityLogResponse> responses = securityLogService.detectSuspiciousActivity();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/login-history/{actorId}")
    public ResponseEntity<ApiResponse<List<SecurityLogResponse>>> getLoginHistory(
            @PathVariable UUID actorId) {
        List<SecurityLogResponse> responses = securityLogService.getLoginHistory(actorId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
