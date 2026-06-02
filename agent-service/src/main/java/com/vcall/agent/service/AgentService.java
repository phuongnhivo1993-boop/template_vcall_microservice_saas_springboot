package com.vcall.agent.service;

import com.vcall.agent.dto.AgentRequest;
import com.vcall.agent.dto.AgentResponse;
import com.vcall.agent.dto.AgentSessionResponse;
import com.vcall.agent.dto.AgentStatusRequest;
import com.vcall.agent.entity.Agent;
import com.vcall.agent.entity.Agent.AgentStatusEnum;
import com.vcall.agent.entity.AgentGroup;
import com.vcall.agent.entity.AgentGroupMember;
import com.vcall.agent.entity.AgentSession;
import com.vcall.agent.kafka.AgentEventPublisher;
import com.vcall.agent.repository.AgentGroupMemberRepository;
import com.vcall.agent.repository.AgentGroupRepository;
import com.vcall.agent.repository.AgentRepository;
import com.vcall.agent.repository.AgentSessionRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final AgentGroupRepository agentGroupRepository;
    private final AgentGroupMemberRepository agentGroupMemberRepository;
    private final AgentSessionRepository agentSessionRepository;
    private final AgentStatusService agentStatusService;
    private final AgentEventPublisher eventPublisher;

    @Transactional
    public AgentResponse createAgent(AgentRequest request) {
        Agent agent = new Agent();
        agent.setUserId(request.getUserId());
        agent.setAgentCode(request.getAgentCode());
        agent.setFullName(request.getFullName());
        agent.setEmail(request.getEmail());
        agent.setPhone(request.getPhone());
        agent.setSkill(request.getSkill());
        agent.setMaxConcurrentCalls(request.getMaxConcurrentCalls());
        agent.setStatus(AgentStatusEnum.OFFLINE);
        agent = agentRepository.save(agent);

        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            assignToGroups(agent, request.getGroupIds());
        }

        eventPublisher.publishAgentCreated(agent);
        return toResponse(agent);
    }

    @Transactional(readOnly = true)
    public AgentResponse getAgent(UUID id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        return toResponse(agent);
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> getAllAgents() {
        return agentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgentResponse updateAgent(UUID id, AgentRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        agent.setFullName(request.getFullName());
        agent.setEmail(request.getEmail());
        agent.setPhone(request.getPhone());
        agent.setSkill(request.getSkill());
        agent.setMaxConcurrentCalls(request.getMaxConcurrentCalls());
        agent = agentRepository.save(agent);
        return toResponse(agent);
    }

    @Transactional
    public void deleteAgent(UUID id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        agentRepository.delete(agent);
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> getByStatus(AgentStatusEnum status) {
        return agentRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgentResponse updateStatus(UUID id, AgentStatusRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        AgentStatusEnum newStatus = AgentStatusEnum.valueOf(request.getStatus().toUpperCase());
        agent.setStatus(newStatus);
        agent = agentRepository.save(agent);

        agentStatusService.updateStatus(agent, newStatus.name(), request.getReason());
        eventPublisher.publishStatusChanged(agent, request.getReason());
        return toResponse(agent);
    }

    @Transactional
    public void assignToGroup(UUID agentId, Long groupId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));
        AgentGroup group = agentGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        boolean alreadyMember = agentGroupMemberRepository.findByAgentId(agentId).stream()
                .anyMatch(m -> m.getGroup().getId().equals(groupId));
        if (!alreadyMember) {
            AgentGroupMember member = new AgentGroupMember();
            member.setAgent(agent);
            member.setGroup(group);
            agentGroupMemberRepository.save(member);
        }
    }

    @Transactional
    public void removeFromGroup(UUID agentId, Long groupId) {
        List<AgentGroupMember> members = agentGroupMemberRepository.findByAgentId(agentId);
        members.stream()
                .filter(m -> m.getGroup().getId().equals(groupId))
                .findFirst()
                .ifPresent(agentGroupMemberRepository::delete);
    }

    @Transactional(readOnly = true)
    public AgentResponse getAgentByUserId(UUID userId) {
        Agent agent = agentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found for userId: " + userId));
        return toResponse(agent);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAgentStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAgents", agentRepository.count());
        stats.put("onlineCount", agentRepository.countByStatus(AgentStatusEnum.ONLINE));
        stats.put("offlineCount", agentRepository.countByStatus(AgentStatusEnum.OFFLINE));
        stats.put("busyCount", agentRepository.countByStatus(AgentStatusEnum.BUSY));
        stats.put("onBreakCount", agentRepository.countByStatus(AgentStatusEnum.BREAK));
        stats.put("awayCount", agentRepository.countByStatus(AgentStatusEnum.AWAY));
        return stats;
    }

    @Transactional(readOnly = true)
    public AgentSessionResponse getCurrentSession(UUID agentId) {
        return agentSessionRepository.findByAgentIdAndLogoutTimeIsNull(agentId)
                .map(this::toSessionResponse)
                .orElse(null);
    }

    private void assignToGroups(Agent agent, Set<Long> groupIds) {
        groupIds.forEach(groupId -> {
            AgentGroup group = agentGroupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
            AgentGroupMember member = new AgentGroupMember();
            member.setAgent(agent);
            member.setGroup(group);
            agentGroupMemberRepository.save(member);
        });
    }

    private AgentResponse toResponse(Agent agent) {
        AgentSessionResponse currentSession = getCurrentSession(agent.getId());
        return AgentResponse.builder()
                .id(agent.getId())
                .userId(agent.getUserId())
                .agentCode(agent.getAgentCode())
                .fullName(agent.getFullName())
                .email(agent.getEmail())
                .phone(agent.getPhone())
                .status(agent.getStatus().name())
                .skill(agent.getSkill())
                .maxConcurrentCalls(agent.getMaxConcurrentCalls())
                .currentSession(currentSession)
                .createdAt(agent.getCreatedAt())
                .build();
    }

    private AgentSessionResponse toSessionResponse(AgentSession session) {
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
