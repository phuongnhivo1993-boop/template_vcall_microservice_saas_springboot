package com.vcall.scheduling.repository;

import com.vcall.scheduling.entity.AgentAvailability;
import com.vcall.scheduling.entity.AgentAvailability.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgentAvailabilityRepository extends JpaRepository<AgentAvailability, UUID>, JpaSpecificationExecutor<AgentAvailability> {

    List<AgentAvailability> findByAgentId(UUID agentId);

    List<AgentAvailability> findByAgentIdAndDate(UUID agentId, LocalDate date);

    List<AgentAvailability> findByAgentIdAndDateBetween(UUID agentId, LocalDate start, LocalDate end);

    List<AgentAvailability> findByAgentIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            UUID agentId, LocalDate date, LocalTime endTime, LocalTime startTime);

    List<AgentAvailability> findByAgentIdAndStatus(UUID agentId, AvailabilityStatus status);

    List<AgentAvailability> findByDate(LocalDate date);
}
