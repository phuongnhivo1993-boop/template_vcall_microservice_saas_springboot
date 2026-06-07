package com.vcall.agent.repository;

import com.vcall.agent.entity.Agent;
import com.vcall.common.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRepository extends TenantAwareRepository<Agent, UUID> {

    Optional<Agent> findByUserId(UUID userId);

    Optional<Agent> findByAgentCode(String agentCode);

    List<Agent> findByStatus(Agent.AgentStatusEnum status);

    long countByStatus(Agent.AgentStatusEnum status);
}
