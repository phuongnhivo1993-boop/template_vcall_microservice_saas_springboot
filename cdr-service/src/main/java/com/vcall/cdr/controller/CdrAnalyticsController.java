package com.vcall.cdr.controller;

import com.vcall.cdr.service.CdrAnalyticsService;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cdr/analytics")
@RequiredArgsConstructor
public class CdrAnalyticsController {

    private final CdrAnalyticsService cdrAnalyticsService;

    @GetMapping("/volume")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCallVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "hourly") String granularity) {
        return ResponseEntity.ok(ApiResponse.success(cdrAnalyticsService.getCallVolume(granularity, startDate, endDate)));
    }

    @GetMapping("/agent-performance")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgentPerformance(
            @RequestParam UUID agentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(cdrAnalyticsService.getAgentPerformance(agentId, startDate, endDate)));
    }

    @GetMapping("/cost")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCostAnalysis(
            @RequestParam UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(cdrAnalyticsService.getCostAnalysis(tenantId, startDate, endDate)));
    }

    @GetMapping("/concurrent-calls")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getConcurrentCalls(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(cdrAnalyticsService.getConcurrentCalls(startDate, endDate)));
    }
}
