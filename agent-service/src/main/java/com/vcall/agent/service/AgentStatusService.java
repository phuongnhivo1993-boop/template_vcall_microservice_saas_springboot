package com.vcall.agent.service;

import com.vcall.agent.dto.AgentStatusResponse;
import com.vcall.agent.entity.Agent;
import com.vcall.agent.entity.AgentStatus;
import com.vcall.agent.repository.AgentStatusRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentStatusService {

    private final AgentStatusRepository agentStatusRepository;

    @Transactional
    public AgentStatusResponse updateStatus(Agent agent, String status, String reason) {
        AgentStatus agentStatus = new AgentStatus();
        agentStatus.setAgent(agent);
        agentStatus.setStatus(status);
        agentStatus.setChangedAt(LocalDateTime.now());
        agentStatus.setReason(reason);
        agentStatus = agentStatusRepository.save(agentStatus);
        return toResponse(agentStatus);
    }

    @Transactional(readOnly = true)
    public AgentStatusResponse getCurrentStatus(UUID agentId) {
        return agentStatusRepository.findTopByAgentIdOrderByChangedAtDesc(agentId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No status found for agent: " + agentId));
    }

    @Transactional(readOnly = true)
    public List<AgentStatusResponse> getStatusHistory(UUID agentId, LocalDateTime start, LocalDateTime end) {
        return agentStatusRepository.findByAgentIdAndChangedAtBetween(agentId, start, end)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AgentStatusResponse toResponse(AgentStatus agentStatus) {
        return AgentStatusResponse.builder()
                .id(agentStatus.getId())
                .agentId(agentStatus.getAgent().getId())
                .status(agentStatus.getStatus())
                .changedAt(agentStatus.getChangedAt())
                .reason(agentStatus.getReason())
                .build();
    }
}
