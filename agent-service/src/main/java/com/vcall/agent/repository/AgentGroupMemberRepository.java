package com.vcall.agent.repository;

import com.vcall.agent.entity.AgentGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentGroupMemberRepository extends JpaRepository<AgentGroupMember, Long> {

    List<AgentGroupMember> findByAgentId(UUID agentId);

    List<AgentGroupMember> findByGroupId(Long groupId);
}
