package com.vcall.reporting.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.reporting.dto.DashboardDataResponse;
import com.vcall.reporting.dto.DashboardWidgetResponse;
import com.vcall.reporting.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActivities() {
        List<Map<String, Object>> activities = dashboardService.getRecentActivities();
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

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

    @GetMapping("/export/csv")
    public void exportDashboardCsv(HttpServletResponse response) throws IOException {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        List<String> headers = Arrays.asList("Metric", "Value");
        List<List<String>> rows = List.of(
                List.of("Total Calls Today", String.valueOf(stats.getOrDefault("totalCallsToday", 0))),
                List.of("Answered Calls", String.valueOf(stats.getOrDefault("answeredCalls", 0))),
                List.of("Missed Calls", String.valueOf(stats.getOrDefault("missedCalls", 0))),
                List.of("Answer Rate", String.valueOf(stats.getOrDefault("answerRate", 0.0))),
                List.of("Active Agents", String.valueOf(stats.getOrDefault("activeAgents", 0))),
                List.of("Avg Occupancy Rate", String.valueOf(stats.getOrDefault("avgOccupancyRate", 0.0))),
                List.of("Avg Satisfaction Score", String.valueOf(stats.getOrDefault("avgSatisfactionScore", 0.0))),
                List.of("Total Report Executions", String.valueOf(stats.getOrDefault("totalReportExecutions", 0))),
                List.of("Report Compliance Rate", String.valueOf(stats.getOrDefault("reportComplianceRate", 0.0)))
        );

        CsvExportUtil.writeCsv(response, "dashboard_" + timestamp + ".csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportDashboardExcel(HttpServletResponse response) throws IOException {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        List<String> headers = Arrays.asList("Metric", "Value");
        List<Map<String, String>> items = List.of(
                Map.of("metric", "Total Calls Today", "value", String.valueOf(stats.getOrDefault("totalCallsToday", 0))),
                Map.of("metric", "Answered Calls", "value", String.valueOf(stats.getOrDefault("answeredCalls", 0))),
                Map.of("metric", "Missed Calls", "value", String.valueOf(stats.getOrDefault("missedCalls", 0))),
                Map.of("metric", "Answer Rate", "value", String.valueOf(stats.getOrDefault("answerRate", 0.0))),
                Map.of("metric", "Active Agents", "value", String.valueOf(stats.getOrDefault("activeAgents", 0))),
                Map.of("metric", "Avg Occupancy Rate", "value", String.valueOf(stats.getOrDefault("avgOccupancyRate", 0.0))),
                Map.of("metric", "Avg Satisfaction Score", "value", String.valueOf(stats.getOrDefault("avgSatisfactionScore", 0.0))),
                Map.of("metric", "Total Report Executions", "value", String.valueOf(stats.getOrDefault("totalReportExecutions", 0))),
                Map.of("metric", "Report Compliance Rate", "value", String.valueOf(stats.getOrDefault("reportComplianceRate", 0.0)))
        );

        ExcelExportUtil.writeExcel(response, "dashboard_" + timestamp + ".xlsx", headers, items,
                Arrays.asList("metric", "value"));
    }
}
