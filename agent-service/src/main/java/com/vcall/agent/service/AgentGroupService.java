package com.vcall.agent.service;

import com.vcall.agent.dto.AgentGroupRequest;
import com.vcall.agent.dto.AgentGroupResponse;
import com.vcall.agent.entity.Agent;
import com.vcall.agent.entity.AgentGroup;
import com.vcall.agent.entity.AgentGroupMember;
import com.vcall.agent.repository.AgentGroupMemberRepository;
import com.vcall.agent.repository.AgentGroupRepository;
import com.vcall.agent.repository.AgentRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class AgentGroupService {

    private final AgentGroupRepository agentGroupRepository;
    private final AgentGroupMemberRepository agentGroupMemberRepository;
    private final AgentRepository agentRepository;

    @Transactional
    public AgentGroupResponse createGroup(AgentGroupRequest request) {
        AgentGroup group = new AgentGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group = agentGroupRepository.save(group);
        return toResponse(group);
    }

    @Transactional(readOnly = true)
    public AgentGroupResponse getGroup(Long id) {
        AgentGroup group = agentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        return toResponse(group);
    }

    @Transactional(readOnly = true)
    public Page<AgentGroupResponse> getAllGroups(Pageable pageable) {
        return agentGroupRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public AgentGroupResponse updateGroup(Long id, AgentGroupRequest request) {
        AgentGroup group = agentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group = agentGroupRepository.save(group);
        return toResponse(group);
    }

    @Transactional
    public void deleteGroup(Long id) {
        AgentGroup group = agentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        agentGroupMemberRepository.findByGroupId(id).forEach(agentGroupMemberRepository::delete);
        agentGroupRepository.delete(group);
    }

    @Transactional
    public void addMember(Long groupId, UUID agentId) {
        AgentGroup group = agentGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));

        boolean alreadyMember = agentGroupMemberRepository.findByGroupId(groupId).stream()
                .anyMatch(m -> m.getAgent().getId().equals(agentId));
        if (!alreadyMember) {
            AgentGroupMember member = new AgentGroupMember();
            member.setAgent(agent);
            member.setGroup(group);
            agentGroupMemberRepository.save(member);
        }
    }

    @Transactional
    public void removeMember(Long groupId, UUID agentId) {
        List<AgentGroupMember> members = agentGroupMemberRepository.findByGroupId(groupId);
        members.stream()
                .filter(m -> m.getAgent().getId().equals(agentId))
                .findFirst()
                .ifPresent(agentGroupMemberRepository::delete);
    }

    private AgentGroupResponse toResponse(AgentGroup group) {
        long memberCount = agentGroupMemberRepository.findByGroupId(group.getId()).size();
        return AgentGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .memberCount(memberCount)
                .build();
    }
}
