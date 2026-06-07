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

    /**
     * Returns dashboard stats aggregated from ReportingDataService.
     * Data sources:
     * - Call metrics: from AgentPerformanceCache (Kafka call.ended events)
     * - Agent metrics: from AgentPerformanceCache (Kafka call.ended events)
     * - Report execution metrics: from ReportExecution table (reporting-service internal)
     *
     * NOTE: Real-time call data and ticket SLA data require direct integration
     * with call-service and ticket-service respectively.
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Object> callReport = reportingDataService.generateCallVolumeReport(new HashMap<>());
        stats.put("totalCallsToday", callReport.getOrDefault("totalCalls", 0L));
        stats.put("answeredCalls", callReport.getOrDefault("answered", 0L));
        stats.put("missedCalls", callReport.getOrDefault("missed", 0L));
        stats.put("answerRate", callReport.getOrDefault("answerRate", 0.0));

        Map<String, Object> agentReport = reportingDataService.generateAgentPerformanceReport(new HashMap<>());
        stats.put("activeAgents", agentReport.getOrDefault("totalAgents", 0));
        stats.put("avgOccupancyRate", agentReport.getOrDefault("avgOccupancyRate", 0.0));
        stats.put("avgSatisfactionScore", agentReport.getOrDefault("avgSatisfactionScore", 0.0));

        Map<String, Object> slaReport = reportingDataService.generateSlaReport(new HashMap<>());
        stats.put("totalReportExecutions", slaReport.getOrDefault("totalExecutions", 0L));
        stats.put("reportComplianceRate", slaReport.getOrDefault("complianceRate", 0.0));

        stats.put("dataSource", "agent-performance-cache");
        stats.put("dataSourceNote", "Call/agent data from Kafka events; report metrics from reporting-service");
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
