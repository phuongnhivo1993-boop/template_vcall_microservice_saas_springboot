package com.vcall.reporting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.reporting.entity.AgentPerformanceCache;
import com.vcall.reporting.entity.ReportExecution;
import com.vcall.reporting.repository.AgentPerformanceCacheRepository;
import com.vcall.reporting.repository.ReportExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingDataService {

    private final ObjectMapper objectMapper;
    private final ReportExecutionRepository reportExecutionRepository;
    private final AgentPerformanceCacheRepository agentPerformanceCacheRepository;

    public Map<String, Object> generateCallVolumeReport(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        String period = (String) params.getOrDefault("period", "today");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<ReportExecution> executions = reportExecutionRepository.findByExecutedAtBetween(startOfDay, endOfDay);

        long totalCalls = executions.size();
        long completed = executions.stream().filter(e -> e.getStatus() == ReportExecution.ExecutionStatus.COMPLETED).count();
        long failed = executions.stream().filter(e -> e.getStatus() == ReportExecution.ExecutionStatus.FAILED).count();
        long pending = executions.stream().filter(e -> e.getStatus() == ReportExecution.ExecutionStatus.PENDING || e.getStatus() == ReportExecution.ExecutionStatus.RUNNING).count();

        result.put("totalCalls", totalCalls);
        result.put("answered", completed);
        result.put("missed", pending);
        result.put("failed", failed);
        result.put("avgDuration", executions.stream()
                .filter(e -> e.getExecutionTime() != null)
                .mapToLong(ReportExecution::getExecutionTime)
                .average()
                .orElse(0.0));
        result.put("answerRate", totalCalls > 0 ? (double) completed / totalCalls * 100 : 0.0);
        result.put("period", period);
        result.put("dataSource", "database");
        return result;
    }

    public Map<String, Object> generateAgentPerformanceReport(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<AgentPerformanceCache> agentCaches = agentPerformanceCacheRepository
                .findByPeriodAndPeriodStart(AgentPerformanceCache.Period.DAILY, today);

        if (agentCaches.isEmpty()) {
            agentCaches = agentPerformanceCacheRepository
                    .findByPeriodStartBetween(today.minusDays(7), today);
        }

        List<Map<String, Object>> agents = agentCaches.stream().map(cache -> {
            Map<String, Object> agentData = new HashMap<>();
            agentData.put("agentId", cache.getAgentId());
            agentData.put("name", cache.getAgentName());
            agentData.put("totalCalls", cache.getTotalCalls());
            agentData.put("answeredCalls", cache.getAnsweredCalls());
            agentData.put("missedCalls", cache.getMissedCalls());
            agentData.put("avgDuration", cache.getAvgTalkDuration());
            agentData.put("satisfaction", cache.getSatisfactionScore());
            agentData.put("occupancyRate", cache.getOccupancyRate());
            return agentData;
        }).collect(Collectors.toList());

        double avgOccupancy = agentCaches.stream()
                .mapToDouble(AgentPerformanceCache::getOccupancyRate)
                .average()
                .orElse(0.0);
        double avgSatisfaction = agentCaches.stream()
                .mapToDouble(AgentPerformanceCache::getSatisfactionScore)
                .average()
                .orElse(0.0);

        result.put("agents", agents);
        result.put("totalAgents", agentCaches.size());
        result.put("avgOccupancyRate", avgOccupancy);
        result.put("avgSatisfactionScore", avgSatisfaction);
        result.put("dataSource", "database");
        return result;
    }

    public Map<String, Object> generateSlaReport(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        String period = (String) params.getOrDefault("period", "today");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<ReportExecution> executions = reportExecutionRepository.findByExecutedAtBetween(startOfDay, endOfDay);
        long total = executions.size();
        long completed = executions.stream()
                .filter(e -> e.getStatus() == ReportExecution.ExecutionStatus.COMPLETED).count();
        long breached = executions.stream()
                .filter(e -> e.getStatus() == ReportExecution.ExecutionStatus.FAILED).count();

        result.put("totalTickets", total);
        result.put("breached", breached);
        result.put("compliant", completed);
        result.put("slaComplianceRate", total > 0 ? (double) completed / total * 100 : 0.0);
        result.put("period", period);
        result.put("dataSource", "database");
        return result;
    }

    public Map<String, Object> generateCostReport(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        String period = (String) params.getOrDefault("period", "today");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<ReportExecution> executions = reportExecutionRepository.findByExecutedAtBetween(startOfDay, endOfDay);

        Map<String, Long> executionsByReportType = executions.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getReportDefinition().getReportType().name(),
                        Collectors.counting()));

        result.put("totalExecutions", (long) executions.size());
        result.put("executionsByReportType", executionsByReportType);
        result.put("avgExecutionTimeMs", executions.stream()
                .filter(e -> e.getExecutionTime() != null)
                .mapToLong(ReportExecution::getExecutionTime)
                .average()
                .orElse(0.0));
        result.put("period", period);
        result.put("dataSource", "database");
        return result;
    }
}
