package com.vcall.reporting.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.reporting.dto.AgentPerformanceResponse;
import com.vcall.reporting.entity.AgentPerformanceCache;
import com.vcall.reporting.repository.AgentPerformanceCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentPerformanceService {

    private final AgentPerformanceCacheRepository agentPerformanceCacheRepository;

    @Transactional(readOnly = true)
    public AgentPerformanceResponse getPerformance(UUID agentId, String period, LocalDate startDate, LocalDate endDate) {
        AgentPerformanceCache.Period perfPeriod = AgentPerformanceCache.Period.valueOf(period.toUpperCase());
        AgentPerformanceCache cache = agentPerformanceCacheRepository
                .findByAgentIdAndPeriodAndPeriodStart(agentId, perfPeriod, startDate)
                .orElseThrow(() -> new ResourceNotFoundException("Agent performance not found for agent: " + agentId));
        return toResponse(cache);
    }

    @Transactional(readOnly = true)
    public List<AgentPerformanceResponse> getPerformanceSummary(String period, LocalDate startDate, LocalDate endDate) {
        AgentPerformanceCache.Period perfPeriod = AgentPerformanceCache.Period.valueOf(period.toUpperCase());
        List<AgentPerformanceCache> caches;
        if (startDate != null && endDate != null) {
            caches = agentPerformanceCacheRepository.findByPeriodStartBetween(startDate, endDate)
                    .stream()
                    .filter(c -> c.getPeriod() == perfPeriod)
                    .toList();
        } else {
            caches = agentPerformanceCacheRepository.findByPeriodAndPeriodStart(perfPeriod, LocalDate.now());
        }
        return caches.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AgentPerformanceCache cachePerformance(AgentPerformanceCache cache) {
        return agentPerformanceCacheRepository.save(cache);
    }

    @Transactional
    public void updatePerformance(UUID agentId, String agentName, AgentPerformanceCache.Period period,
                                   LocalDate periodStart, long totalCalls, long answeredCalls,
                                   long missedCalls, double avgTalkDuration, double avgWaitDuration,
                                   long totalTalkTime, int maxConcurrentCalls, double satisfactionScore,
                                   double occupancyRate) {
        AgentPerformanceCache cache = agentPerformanceCacheRepository
                .findByAgentIdAndPeriodAndPeriodStart(agentId, period, periodStart)
                .orElseGet(() -> {
                    AgentPerformanceCache newCache = new AgentPerformanceCache();
                    newCache.setAgentId(agentId);
                    newCache.setAgentName(agentName);
                    newCache.setPeriod(period);
                    newCache.setPeriodStart(periodStart);
                    return newCache;
                });

        cache.setTotalCalls(cache.getTotalCalls() + totalCalls);
        cache.setAnsweredCalls(cache.getAnsweredCalls() + answeredCalls);
        cache.setMissedCalls(cache.getMissedCalls() + missedCalls);
        cache.setAvgTalkDuration(avgTalkDuration);
        cache.setAvgWaitDuration(avgWaitDuration);
        cache.setTotalTalkTime(cache.getTotalTalkTime() + totalTalkTime);
        cache.setMaxConcurrentCalls(Math.max(cache.getMaxConcurrentCalls(), maxConcurrentCalls));
        cache.setSatisfactionScore(satisfactionScore);
        cache.setOccupancyRate(occupancyRate);
        agentPerformanceCacheRepository.save(cache);
    }

    private AgentPerformanceResponse toResponse(AgentPerformanceCache cache) {
        return AgentPerformanceResponse.builder()
                .agentId(cache.getAgentId())
                .agentName(cache.getAgentName())
                .period(cache.getPeriod().name())
                .totalCalls(cache.getTotalCalls())
                .answeredCalls(cache.getAnsweredCalls())
                .missedCalls(cache.getMissedCalls())
                .avgTalkDuration(cache.getAvgTalkDuration())
                .occupancyRate(cache.getOccupancyRate())
                .build();
    }
}
