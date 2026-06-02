package com.vcall.reporting.repository;

import com.vcall.reporting.entity.AgentPerformanceCache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentPerformanceCacheRepository extends JpaRepository<AgentPerformanceCache, Long> {

    Optional<AgentPerformanceCache> findByAgentIdAndPeriodAndPeriodStart(UUID agentId, AgentPerformanceCache.Period period, LocalDate periodStart);

    List<AgentPerformanceCache> findByPeriodAndPeriodStart(AgentPerformanceCache.Period period, LocalDate periodStart);
    Page<AgentPerformanceCache> findByPeriodAndPeriodStart(AgentPerformanceCache.Period period, LocalDate periodStart, Pageable pageable);

    List<AgentPerformanceCache> findByPeriodStartBetween(LocalDate start, LocalDate end);
    Page<AgentPerformanceCache> findByPeriodStartBetween(LocalDate start, LocalDate end, Pageable pageable);
}
