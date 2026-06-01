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
