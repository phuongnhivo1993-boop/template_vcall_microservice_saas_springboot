package com.vcall.pbx.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.pbx.dto.PbxQueueRequest;
import com.vcall.pbx.dto.PbxQueueResponse;
import com.vcall.pbx.entity.Extension;
import com.vcall.pbx.entity.PbxQueue;
import com.vcall.pbx.entity.PbxQueue.QueueStrategy;
import com.vcall.pbx.entity.PbxQueue.TimeoutAction;
import com.vcall.pbx.entity.PbxQueueMember;
import com.vcall.pbx.repository.ExtensionRepository;
import com.vcall.pbx.repository.PbxQueueMemberRepository;
import com.vcall.pbx.repository.PbxQueueRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PbxQueueService {

    private final PbxQueueRepository pbxQueueRepository;
    private final PbxQueueMemberRepository pbxQueueMemberRepository;
    private final ExtensionRepository extensionRepository;

    @Transactional
    public PbxQueueResponse createQueue(PbxQueueRequest request) {
        if (pbxQueueRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Queue already exists with name: " + request.getName());
        }

        PbxQueue queue = new PbxQueue();
        queue.setName(request.getName());
        queue.setDescription(request.getDescription());
        queue.setStrategy(QueueStrategy.valueOf(request.getStrategy().toUpperCase()));
        queue.setMaxWaitTime(request.getMaxWaitTime() != null ? request.getMaxWaitTime() : 300);
        queue.setMaxQueueSize(request.getMaxQueueSize() != null ? request.getMaxQueueSize() : 50);
        if (request.getTimeoutAction() != null) {
            queue.setTimeoutAction(TimeoutAction.valueOf(request.getTimeoutAction().toUpperCase()));
        }
        queue.setTimeoutDestination(request.getTimeoutDestination());
        queue = pbxQueueRepository.save(queue);
        return toResponse(queue);
    }

    @Transactional(readOnly = true)
    public PbxQueueResponse getQueue(Long id) {
        PbxQueue queue = pbxQueueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));
        return toResponse(queue);
    }

    @Transactional(readOnly = true)
    public Page<PbxQueueResponse> getAllQueues(Pageable pageable) {
        return pbxQueueRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public PbxQueueResponse updateQueue(Long id, PbxQueueRequest request) {
        PbxQueue queue = pbxQueueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));
        queue.setName(request.getName());
        queue.setDescription(request.getDescription());
        queue.setStrategy(QueueStrategy.valueOf(request.getStrategy().toUpperCase()));
        queue.setMaxWaitTime(request.getMaxWaitTime());
        queue.setMaxQueueSize(request.getMaxQueueSize());
        if (request.getTimeoutAction() != null) {
            queue.setTimeoutAction(TimeoutAction.valueOf(request.getTimeoutAction().toUpperCase()));
        }
        queue.setTimeoutDestination(request.getTimeoutDestination());
        queue = pbxQueueRepository.save(queue);
        return toResponse(queue);
    }

    @Transactional
    public void deleteQueue(Long id) {
        PbxQueue queue = pbxQueueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));
        pbxQueueMemberRepository.deleteAll(pbxQueueMemberRepository.findByQueueId(id));
        queue.setIsDeleted(true);
        pbxQueueRepository.save(queue);
    }

    @Transactional
    public void addMember(Long queueId, Long extensionId, Integer priority) {
        PbxQueue queue = pbxQueueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + queueId));
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found with id: " + extensionId));

        PbxQueueMember member = new PbxQueueMember();
        member.setQueue(queue);
        member.setExtension(extension);
        member.setPriority(priority != null ? priority : 0);
        pbxQueueMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long memberId) {
        PbxQueueMember member = pbxQueueMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue member not found with id: " + memberId));
        pbxQueueMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public Page<PbxQueueResponse> search(String keyword, String strategy, Pageable pageable) {
        Specification<PbxQueue> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (strategy != null && !strategy.isEmpty()) {
                predicates.add(cb.equal(root.get("strategy"), QueueStrategy.valueOf(strategy.toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return pbxQueueRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        List<PbxQueue> all = pbxQueueRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) all.size());
        stats.put("fifo", all.stream().filter(q -> q.getStrategy() == QueueStrategy.FIFO).count());
        stats.put("roundRobin", all.stream().filter(q -> q.getStrategy() == QueueStrategy.ROUND_ROBIN).count());
        stats.put("ringAll", all.stream().filter(q -> q.getStrategy() == QueueStrategy.RING_ALL).count());
        return stats;
    }

    private PbxQueueResponse toResponse(PbxQueue queue) {
        int memberCount = pbxQueueMemberRepository.findByQueueId(queue.getId()).size();
        return PbxQueueResponse.builder()
                .id(queue.getId())
                .name(queue.getName())
                .description(queue.getDescription())
                .strategy(queue.getStrategy().name())
                .maxWaitTime(queue.getMaxWaitTime())
                .maxQueueSize(queue.getMaxQueueSize())
                .timeoutAction(queue.getTimeoutAction() != null ? queue.getTimeoutAction().name() : null)
                .timeoutDestination(queue.getTimeoutDestination())
                .memberCount(memberCount)
                .build();
    }
}
