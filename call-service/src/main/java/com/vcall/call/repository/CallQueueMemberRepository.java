package com.vcall.call.repository;

import com.vcall.call.entity.CallQueueMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CallQueueMemberRepository extends JpaRepository<CallQueueMember, Long> {

    List<CallQueueMember> findByQueueId(Long queueId);

    List<CallQueueMember> findByAgentId(UUID agentId);
}
