package com.vcall.call.service;

import com.vcall.call.dto.CallRequest;
import com.vcall.call.dto.CallResponse;
import com.vcall.call.dto.RoutingRuleRequest;
import com.vcall.call.dto.RoutingRuleResponse;
import com.vcall.call.entity.CallRoutingRule;
import com.vcall.call.repository.CallRoutingRuleRepository;
import com.vcall.common.exception.ResourceNotFoundException;
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

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final CallRoutingRuleRepository routingRuleRepository;
    private final CallService callService;
    private final QueueService queueService;
    private final IvrFlowService ivrFlowService;

    @Transactional(readOnly = true)
    public CallRoutingRule findMatchingRule(CallRequest request) {
        List<CallRoutingRule> activeRules = routingRuleRepository.findByIsActiveTrueOrderByPriority();
        for (CallRoutingRule rule : activeRules) {
            if (matchesCondition(rule, request)) {
                return rule;
            }
        }
        return null;
    }

    @Transactional
    public CallResponse routeCall(CallRequest request) {
        CallRoutingRule matchingRule = findMatchingRule(request);

        if (matchingRule != null) {
            switch (matchingRule.getDestination()) {
                case "queue":
                    request.setQueueId(matchingRule.getDestinationId());
                    break;
                case "ivr":
                    request.setIvrFlowId(matchingRule.getDestinationId());
                    break;
                case "agent":
                    break;
                case "extension":
                    break;
                default:
                    break;
            }
        }

        CallResponse call = callService.createCall(request);

        if (call.getQueueId() != null) {
            callService.updateCallStatus(call.getId(),
                    com.vcall.call.dto.CallStatusRequest.builder()
                            .status("RINGING")
                            .build());
        }

        return call;
    }

    @Transactional(readOnly = true)
    public Page<RoutingRuleResponse> findAll(Pageable pageable) {
        return routingRuleRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RoutingRuleResponse findById(Long id) {
        CallRoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));
        return toResponse(rule);
    }

    @Transactional
    public RoutingRuleResponse create(RoutingRuleRequest request) {
        CallRoutingRule rule = new CallRoutingRule();
        rule.setName(request.getName());
        rule.setPriority(request.getPriority());
        rule.setCondition(request.getCondition());
        rule.setDestination(request.getDestination());
        rule.setDestinationId(request.getDestinationId());
        rule.setTimeProfile(request.getTimeProfile());
        rule.setActive(request.isActive());
        rule = routingRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public RoutingRuleResponse update(Long id, RoutingRuleRequest request) {
        CallRoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));
        rule.setName(request.getName());
        rule.setPriority(request.getPriority());
        rule.setCondition(request.getCondition());
        rule.setDestination(request.getDestination());
        rule.setDestinationId(request.getDestinationId());
        rule.setTimeProfile(request.getTimeProfile());
        rule.setActive(request.isActive());
        rule = routingRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public void delete(Long id) {
        CallRoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));
        routingRuleRepository.delete(rule);
    }

    @Transactional(readOnly = true)
    public Page<RoutingRuleResponse> search(String keyword, String destination, Boolean isActive, Pageable pageable) {
        Specification<CallRoutingRule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("condition")), pattern)
                ));
            }
            if (destination != null && !destination.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("destination")), destination.toLowerCase()));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return routingRuleRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        List<CallRoutingRule> all = routingRuleRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) all.size());
        stats.put("active", all.stream().filter(CallRoutingRule::isActive).count());
        stats.put("inactive", all.stream().filter(r -> !r.isActive()).count());
        return stats;
    }

    private boolean matchesCondition(CallRoutingRule rule, CallRequest request) {
        if (rule.getCondition() == null || rule.getCondition().isBlank()) {
            return true;
        }
        return true;
    }

    private RoutingRuleResponse toResponse(CallRoutingRule rule) {
        return RoutingRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .priority(rule.getPriority())
                .condition(rule.getCondition())
                .destination(rule.getDestination())
                .destinationId(rule.getDestinationId())
                .timeProfile(rule.getTimeProfile())
                .isActive(rule.isActive())
                .build();
    }
}
