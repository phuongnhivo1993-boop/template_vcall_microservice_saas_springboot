package com.vcall.agent.service;

import com.vcall.agent.dto.AgentSessionRequest;
import com.vcall.agent.dto.AgentSessionResponse;
import com.vcall.agent.entity.Agent;
import com.vcall.agent.entity.AgentSession;
import com.vcall.agent.kafka.AgentEventPublisher;
import com.vcall.agent.repository.AgentRepository;
import com.vcall.agent.repository.AgentSessionRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentSessionService {

    private final AgentSessionRepository agentSessionRepository;
    private final AgentRepository agentRepository;
    private final AgentEventPublisher eventPublisher;

    @Transactional
    public AgentSessionResponse startSession(AgentSessionRequest request) {
        Agent agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + request.getAgentId()));

        agentSessionRepository.findByAgentIdAndLogoutTimeIsNull(agent.getId())
                .ifPresent(s -> {
                    throw new IllegalStateException("Agent already has an active session");
                });

        AgentSession session = new AgentSession();
        session.setAgent(agent);
        session.setLoginTime(LocalDateTime.now());
        session.setSessionType(request.getSessionType());
        session = agentSessionRepository.save(session);

        eventPublisher.publishSessionStarted(session);
        return toResponse(session);
    }

    @Transactional
    public AgentSessionResponse endSession(Long sessionId) {
        AgentSession session = agentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        if (session.getLogoutTime() != null) {
            throw new IllegalStateException("Session already ended");
        }

        session.setLogoutTime(LocalDateTime.now());
        session.setDuration(ChronoUnit.SECONDS.between(session.getLoginTime(), session.getLogoutTime()));
        session = agentSessionRepository.save(session);

        eventPublisher.publishSessionEnded(session);
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public AgentSessionResponse getActiveSession(UUID agentId) {
        return agentSessionRepository.findByAgentIdAndLogoutTimeIsNull(agentId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for agent: " + agentId));
    }

    @Transactional(readOnly = true)
    public List<AgentSessionResponse> getDailyReport(UUID agentId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return agentSessionRepository.findByAgentIdAndLoginTimeBetween(agentId, startOfDay, endOfDay)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AgentSessionResponse toResponse(AgentSession session) {
        return AgentSessionResponse.builder()
                .id(session.getId())
                .agentId(session.getAgent().getId())
                .loginTime(session.getLoginTime())
                .logoutTime(session.getLogoutTime())
                .duration(session.getDuration())
                .sessionType(session.getSessionType())
                .build();
    }
}
