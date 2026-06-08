package com.vcall.reporting.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.reporting.dto.AgentPerformanceResponse;
import com.vcall.reporting.service.AgentPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/reports/agent-performance")
@RequiredArgsConstructor
public class AgentPerformanceController {

    private final AgentPerformanceService agentPerformanceService;

    @GetMapping("/{agentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AgentPerformanceResponse>> getAgentPerformance(
            @PathVariable UUID agentId,
            @RequestParam(defaultValue = "DAILY") String period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        AgentPerformanceResponse response = agentPerformanceService.getPerformance(agentId, period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AgentPerformanceResponse>>> getPerformanceSummary(
            @RequestParam(defaultValue = "DAILY") String period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        List<AgentPerformanceResponse> responses = agentPerformanceService.getPerformanceSummary(period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
