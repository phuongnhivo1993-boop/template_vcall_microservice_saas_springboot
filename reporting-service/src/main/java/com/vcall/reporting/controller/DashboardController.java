package com.vcall.reporting.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.reporting.dto.DashboardDataResponse;
import com.vcall.reporting.dto.DashboardWidgetResponse;
import com.vcall.reporting.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DashboardDataResponse>>> getDashboardData() {
        List<DashboardDataResponse> responses = dashboardService.getDashboardData();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/widgets")
    public ResponseEntity<ApiResponse<List<DashboardWidgetResponse>>> getWidgets() {
        List<DashboardWidgetResponse> responses = dashboardService.getWidgets();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/widgets/{id}")
    public ResponseEntity<ApiResponse<DashboardDataResponse>> getWidgetData(@PathVariable Long id) {
        DashboardDataResponse response = dashboardService.getWidgetData(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
