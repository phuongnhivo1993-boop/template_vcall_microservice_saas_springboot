package com.vcall.call.service;

import com.vcall.call.dto.QueueRequest;
import com.vcall.call.dto.QueueResponse;
import com.vcall.call.entity.Call;
import com.vcall.call.entity.CallQueue;
import com.vcall.call.entity.CallQueueMember;
import com.vcall.call.repository.CallQueueMemberRepository;
import com.vcall.call.repository.CallQueueRepository;
import com.vcall.call.repository.CallRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final CallQueueRepository queueRepository;
    private final CallQueueMemberRepository memberRepository;
    private final CallRepository callRepository;

    @Transactional
    public QueueResponse createQueue(QueueRequest request) {
        CallQueue queue = new CallQueue();
        queue.setName(request.getName());
        if (request.getStrategy() != null) {
            queue.setStrategy(CallQueue.QueueStrategy.valueOf(request.getStrategy().toUpperCase()));
        }
        queue.setMaxWaitTime(request.getMaxWaitTime());
        queue.setMaxQueueSize(request.getMaxQueueSize());
        queue = queueRepository.save(queue);
        return toResponse(queue);
    }

    @Transactional(readOnly = true)
    public QueueResponse getQueue(Long id) {
        CallQueue queue = queueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));
        return toResponse(queue);
    }

    @Transactional(readOnly = true)
    public List<QueueResponse> getAllQueues() {
        return queueRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public QueueResponse updateQueue(Long id, QueueRequest request) {
        CallQueue queue = queueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));
        queue.setName(request.getName());
        if (request.getStrategy() != null) {
            queue.setStrategy(CallQueue.QueueStrategy.valueOf(request.getStrategy().toUpperCase()));
        }
        queue.setMaxWaitTime(request.getMaxWaitTime());
        queue.setMaxQueueSize(request.getMaxQueueSize());
        queue = queueRepository.save(queue);
        return toResponse(queue);
    }

    @Transactional
    public void deleteQueue(Long id) {
        CallQueue queue = queueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));
        queue.setIsDeleted(true);
        queueRepository.save(queue);
    }

    @Transactional
    public void addAgentToQueue(Long queueId, UUID agentId, Integer priority) {
        CallQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + queueId));
        CallQueueMember member = new CallQueueMember();
        member.setQueue(queue);
        member.setAgentId(agentId);
        member.setPriority(priority != null ? priority : 0);
        memberRepository.save(member);
    }

    @Transactional
    public void removeAgentFromQueue(Long queueId, UUID agentId) {
        List<CallQueueMember> members = memberRepository.findByQueueId(queueId);
        members.stream()
                .filter(m -> m.getAgentId().equals(agentId))
                .findFirst()
                .ifPresent(memberRepository::delete);
    }

    public UUID findBestAgentForCall(Long queueId) {
        CallQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + queueId));
        List<CallQueueMember> members = memberRepository.findByQueueId(queueId);

        if (members.isEmpty()) {
            return null;
        }

        switch (queue.getStrategy()) {
            case ROUND_ROBIN:
                return roundRobinStrategy(members);
            case LEAST_BUSY:
                return leastBusyStrategy(members);
            case RANDOM:
                return randomStrategy(members);
            case RING_ALL:
            default:
                return ringAllStrategy(members);
        }
    }

    private UUID ringAllStrategy(List<CallQueueMember> members) {
        return members.stream()
                .max(Comparator.comparingInt(CallQueueMember::getPriority))
                .map(CallQueueMember::getAgentId)
                .orElse(null);
    }

    private UUID roundRobinStrategy(List<CallQueueMember> members) {
        List<CallQueueMember> sorted = members.stream()
                .sorted(Comparator.comparingInt(CallQueueMember::getPriority).reversed())
                .collect(Collectors.toList());
        return sorted.isEmpty() ? null : sorted.get(0).getAgentId();
    }

    private UUID leastBusyStrategy(List<CallQueueMember> members) {
        return members.stream()
                .min(Comparator.comparingLong(m ->
                        callRepository.findByAgentIdAndStatus(m.getAgentId(), Call.CallStatus.IN_PROGRESS).size()))
                .map(CallQueueMember::getAgentId)
                .orElse(null);
    }

    private UUID randomStrategy(List<CallQueueMember> members) {
        if (members.isEmpty()) return null;
        return members.get(new Random().nextInt(members.size())).getAgentId();
    }

    private QueueResponse toResponse(CallQueue queue) {
        long memberCount = memberRepository.findByQueueId(queue.getId()).size();
        return QueueResponse.builder()
                .id(queue.getId())
                .name(queue.getName())
                .strategy(queue.getStrategy().name())
                .maxWaitTime(queue.getMaxWaitTime())
                .maxQueueSize(queue.getMaxQueueSize())
                .memberCount(memberCount)
                .build();
    }
}
