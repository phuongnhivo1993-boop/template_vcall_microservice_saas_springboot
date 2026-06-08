package com.vcall.automation.controller;

import com.vcall.automation.dto.ExecutionLogResponse;
import com.vcall.automation.service.ExecutionLogService;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/automation/rules/{ruleId}/executions")
@RequiredArgsConstructor
public class ExecutionLogController {

    private final ExecutionLogService executionLogService;

    @GetMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<ExecutionLogResponse>>> getLogs(
            @PathVariable Long ruleId) {
        List<ExecutionLogResponse> logs = executionLogService.getLogs(ruleId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ExecutionLogResponse>> getLog(@PathVariable Long id) {
        ExecutionLogResponse log = executionLogService.getLog(id);
        return ResponseEntity.ok(ApiResponse.success(log));
    }
}
