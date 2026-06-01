package com.vcall.agent.repository;

import com.vcall.agent.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRepository extends JpaRepository<Agent, UUID>, JpaSpecificationExecutor<Agent> {

    Optional<Agent> findByUserId(UUID userId);

    Optional<Agent> findByAgentCode(String agentCode);

    List<Agent> findByStatus(Agent.AgentStatusEnum status);

    long countByStatus(Agent.AgentStatusEnum status);
}
