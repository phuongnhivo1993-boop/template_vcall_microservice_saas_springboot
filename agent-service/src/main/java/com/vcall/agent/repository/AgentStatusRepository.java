package com.vcall.agent.repository;

import com.vcall.agent.entity.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentStatusRepository extends JpaRepository<AgentStatus, Long> {

    Optional<AgentStatus> findTopByAgentIdOrderByChangedAtDesc(UUID agentId);

    List<AgentStatus> findByAgentIdAndChangedAtBetween(UUID agentId, LocalDateTime start, LocalDateTime end);
}
