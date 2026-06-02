package com.vcall.agent.repository;

import com.vcall.agent.entity.AgentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentSessionRepository extends JpaRepository<AgentSession, Long>, JpaSpecificationExecutor<AgentSession> {

    Optional<AgentSession> findByAgentIdAndLogoutTimeIsNull(UUID agentId);

    List<AgentSession> findByAgentIdAndLoginTimeBetween(UUID agentId, LocalDateTime start, LocalDateTime end);
}
