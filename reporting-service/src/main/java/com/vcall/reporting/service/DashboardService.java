package com.vcall.reporting.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.reporting.dto.DashboardDataResponse;
import com.vcall.reporting.dto.DashboardWidgetResponse;
import com.vcall.reporting.entity.DashboardWidget;
import com.vcall.reporting.repository.DashboardWidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardWidgetRepository dashboardWidgetRepository;
    private final ReportingDataService reportingDataService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Object> callReport = reportingDataService.generateCallVolumeReport(new HashMap<>());
        stats.put("totalCallsToday", callReport.get("totalCalls"));
        stats.put("answered", callReport.get("answered"));
        stats.put("missed", callReport.get("missed"));
        stats.put("failed", callReport.get("failed"));
        stats.put("answerRate", callReport.get("answerRate"));

        Map<String, Object> agentReport = reportingDataService.generateAgentPerformanceReport(new HashMap<>());
        stats.put("activeAgents", agentReport.get("totalAgents"));
        stats.put("avgOccupancyRate", agentReport.get("avgOccupancyRate"));
        stats.put("avgSatisfactionScore", agentReport.get("avgSatisfactionScore"));

        Map<String, Object> slaReport = reportingDataService.generateSlaReport(new HashMap<>());
        stats.put("openTickets", slaReport.get("totalTickets"));
        stats.put("slaComplianceRate", slaReport.get("slaComplianceRate"));

        stats.put("dataSource", "database");
        return stats;
    }

    public List<Map<String, Object>> getRecentActivities() {
        return new ArrayList<>();
    }

    @Transactional(readOnly = true)
    public List<DashboardWidgetResponse> getWidgets() {
        return dashboardWidgetRepository.findByIsActiveTrueOrderByPosition()
                .stream()
                .map(this::toWidgetResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardDataResponse> getDashboardData() {
        List<DashboardWidget> widgets = dashboardWidgetRepository.findByIsActiveTrueOrderByPosition();
        return widgets.stream()
                .map(this::executeWidget)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardDataResponse getWidgetData(Long widgetId) {
        DashboardWidget widget = dashboardWidgetRepository.findById(widgetId)
                .orElseThrow(() -> new RuntimeException("Dashboard widget not found with id: " + widgetId));
        return executeWidget(widget);
    }

    private DashboardDataResponse executeWidget(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        try {
            data = switch (widget.getWidgetType()) {
                case CALL_VOLUME, NUMBER_CARD ->
                    reportingDataService.generateCallVolumeReport(new HashMap<>());
                case AGENT_PERFORMANCE, TABLE ->
                    reportingDataService.generateAgentPerformanceReport(new HashMap<>());
                case SLA_COMPLIANCE, PIE_CHART ->
                    reportingDataService.generateSlaReport(new HashMap<>());
                case COST_ANALYSIS, BAR_CHART ->
                    reportingDataService.generateCostReport(new HashMap<>());
                default -> Map.of("message", "Widget type not supported");
            };

            if (widget.getDataQuery() != null && !widget.getDataQuery().isBlank()) {
                try {
                    Map<String, Object> queryParams = objectMapper.readValue(widget.getDataQuery(), new TypeReference<>() {});
                    data.putAll(queryParams);
                } catch (Exception e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            data.put("error", e.getMessage());
        }

        return DashboardDataResponse.builder()
                .widgetId(widget.getId())
                .widgetName(widget.getName())
                .widgetType(widget.getWidgetType().name())
                .data(data)
                .config(widget.getConfig())
                .build();
    }

    private DashboardWidgetResponse toWidgetResponse(DashboardWidget widget) {
        return DashboardWidgetResponse.builder()
                .id(widget.getId())
                .name(widget.getName())
                .widgetType(widget.getWidgetType().name())
                .config(widget.getConfig())
                .position(widget.getPosition())
                .build();
    }
}
