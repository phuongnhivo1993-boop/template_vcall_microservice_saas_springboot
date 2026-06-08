package com.vcall.reporting.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.reporting.dto.CallVolumeReport;
import com.vcall.reporting.dto.SlaReport;
import com.vcall.reporting.service.ReportingDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/reports/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ReportingDataService reportingDataService;

    @GetMapping("/call-volume")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCallVolume(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String granularity) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate != null ? startDate.toString() : null);
        params.put("endDate", endDate != null ? endDate.toString() : null);
        params.put("granularity", granularity);
        Map<String, Object> data = reportingDataService.generateCallVolumeReport(params);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/sla")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlaReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate != null ? startDate.toString() : null);
        params.put("endDate", endDate != null ? endDate.toString() : null);
        Map<String, Object> data = reportingDataService.generateSlaReport(params);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
