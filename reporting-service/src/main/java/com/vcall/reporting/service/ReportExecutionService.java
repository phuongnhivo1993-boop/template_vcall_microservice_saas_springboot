package com.vcall.reporting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.reporting.dto.ReportExecutionResponse;
import com.vcall.reporting.entity.ReportDefinition;
import com.vcall.reporting.entity.ReportExecution;
import com.vcall.reporting.repository.ReportExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportExecutionService {

    private final ReportExecutionRepository reportExecutionRepository;
    private final ReportingDataService reportingDataService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReportExecutionResponse runReport(ReportDefinition report) {
        ReportExecution execution = new ReportExecution();
        execution.setReportDefinition(report);
        execution.setExecutedAt(LocalDateTime.now());
        execution.setStatus(ReportExecution.ExecutionStatus.RUNNING);
        execution.setTriggeredBy(ReportExecution.TriggeredBy.MANUAL);
        execution = reportExecutionRepository.save(execution);

        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> result = executeQuery(report);
            execution.setResultData(objectMapper.writeValueAsString(result));
            execution.setStatus(ReportExecution.ExecutionStatus.COMPLETED);
        } catch (Exception e) {
            execution.setStatus(ReportExecution.ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
        }
        execution.setExecutionTime(System.currentTimeMillis() - startTime);
        execution = reportExecutionRepository.save(execution);
        return toResponse(execution);
    }

    private Map<String, Object> executeQuery(ReportDefinition report) {
        Map<String, Object> params = new HashMap<>();
        if (report.getParameters() != null) {
            try {
                params = objectMapper.readValue(report.getParameters(), Map.class);
            } catch (Exception e) {
                // ignore parse errors
            }
        }
        return switch (report.getReportType()) {
            case CALL_VOLUME -> reportingDataService.generateCallVolumeReport(params);
            case AGENT_PERFORMANCE -> reportingDataService.generateAgentPerformanceReport(params);
            case SLA_COMPLIANCE -> reportingDataService.generateSlaReport(params);
            case COST_ANALYSIS -> reportingDataService.generateCostReport(params);
            default -> Map.of("message", "Report type not supported yet");
        };
    }

    @Transactional(readOnly = true)
    public ReportExecutionResponse getExecution(Long id) {
        ReportExecution execution = getExecutionEntity(id);
        return toResponse(execution);
    }

    @Transactional(readOnly = true)
    public ReportExecution getExecutionEntity(Long id) {
        return reportExecutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report execution not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getExecutionData(Long id) {
        ReportExecution execution = getExecutionEntity(id);
        if (execution.getResultData() != null) {
            try {
                return objectMapper.readValue(execution.getResultData(), Map.class);
            } catch (Exception e) {
                return Map.of("raw", execution.getResultData());
            }
        }
        return Map.of();
    }

    @Transactional(readOnly = true)
    public Page<ReportExecutionResponse> getExecutionHistory(Long reportDefinitionId, Pageable pageable) {
        return reportExecutionRepository.findByReportDefinitionIdOrderByExecutedAtDesc(reportDefinitionId, pageable)
                .map(this::toResponse);
    }

    public void scheduleReports() {
        List<ReportDefinition> scheduledReports = reportExecutionRepository.findAll().stream()
                .map(ReportExecution::getReportDefinition)
                .distinct()
                .toList();
        // Schedule logic would integrate with a scheduler service
    }

    private ReportExecutionResponse toResponse(ReportExecution execution) {
        return ReportExecutionResponse.builder()
                .id(execution.getId())
                .reportName(execution.getReportDefinition().getName())
                .executedAt(execution.getExecutedAt())
                .status(execution.getStatus().name())
                .errorMessage(execution.getErrorMessage())
                .executionTime(execution.getExecutionTime())
                .build();
    }
}
