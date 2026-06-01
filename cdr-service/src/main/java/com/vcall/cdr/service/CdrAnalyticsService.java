package com.vcall.cdr.service;

import com.vcall.cdr.repository.CdrClickHouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CdrAnalyticsService {

    private final CdrClickHouseRepository cdrClickHouseRepository;

    public List<Map<String, Object>> getCallVolume(String granularity, LocalDateTime startDate, LocalDateTime endDate) {
        if ("daily".equalsIgnoreCase(granularity)) {
            return cdrClickHouseRepository.getCallVolumeByDay(startDate, endDate);
        }
        return cdrClickHouseRepository.getCallVolumeByHour(startDate, endDate);
    }

    public List<Map<String, Object>> getAgentPerformance(UUID agentId, LocalDateTime startDate, LocalDateTime endDate) {
        return cdrClickHouseRepository.getAgentPerformanceSummary(agentId, startDate, endDate);
    }

    public List<Map<String, Object>> getCostAnalysis(UUID tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return cdrClickHouseRepository.getCostByTenant(tenantId, startDate, endDate);
    }

    public List<Map<String, Object>> getConcurrentCalls(LocalDateTime startDate, LocalDateTime endDate) {
        return cdrClickHouseRepository.getConcurrentCalls(startDate, endDate);
    }
}
