package com.vcall.automation.service;

import com.vcall.automation.dto.AutomationRuleRequest;
import com.vcall.automation.dto.AutomationRuleResponse;
import com.vcall.automation.entity.AutomationRule;
import com.vcall.automation.repository.AutomationRuleRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AutomationRuleService {

    private final AutomationRuleRepository automationRuleRepository;

    @Transactional(readOnly = true)
    public Page<AutomationRuleResponse> getAllRules(String name, Boolean isActive, Pageable pageable) {
        Specification<AutomationRule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return automationRuleRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AutomationRuleResponse getRule(Long id) {
        AutomationRule rule = automationRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));
        return toResponse(rule);
    }

    @Transactional
    public AutomationRuleResponse createRule(AutomationRuleRequest request) {
        AutomationRule rule = new AutomationRule();
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setTrigger(request.getTrigger());
        rule.setAction(request.getAction());
        rule.setIsActive(request.getIsActive() != null ? request.getIsActive() : false);
        rule.setPriority(0);
        rule.setExecutionCount(0);
        rule = automationRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public AutomationRuleResponse updateRule(Long id, AutomationRuleRequest request) {
        AutomationRule rule = automationRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setTrigger(request.getTrigger());
        rule.setAction(request.getAction());
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }
        rule = automationRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        AutomationRule rule = automationRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));
        rule.setIsDeleted(true);
        automationRuleRepository.save(rule);
    }

    @Transactional
    public AutomationRuleResponse duplicateRule(Long id) {
        AutomationRule original = automationRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));
        AutomationRule copy = new AutomationRule();
        copy.setName(original.getName() + " (Copy)");
        copy.setDescription(original.getDescription());
        copy.setTrigger(original.getTrigger());
        copy.setAction(original.getAction());
        copy.setIsActive(false);
        copy.setPriority(original.getPriority());
        copy.setExecutionCount(0);
        copy = automationRuleRepository.save(copy);
        return toResponse(copy);
    }

    @Transactional
    public AutomationRuleResponse toggleRule(Long id, boolean isActive) {
        AutomationRule rule = automationRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));
        rule.setIsActive(isActive);
        rule = automationRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public AutomationRuleResponse executeRule(Long id) {
        AutomationRule rule = automationRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));
        rule.setExecutionCount(rule.getExecutionCount() + 1);
        rule.setLastExecutedAt(LocalDateTime.now());
        rule = automationRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public void bulkDeleteRules(List<Long> ids) {
        List<AutomationRule> rules = automationRuleRepository.findAllById(ids);
        for (AutomationRule rule : rules) {
            rule.setIsDeleted(true);
        }
        automationRuleRepository.saveAll(rules);
    }

    @Transactional(readOnly = true)
    public Page<AutomationRuleResponse> searchRules(String name, String trigger, String action,
                                                     Boolean isActive, Pageable pageable) {
        Specification<AutomationRule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (trigger != null && !trigger.isEmpty()) {
                predicates.add(cb.like(root.get("trigger"), "%" + trigger + "%"));
            }
            if (action != null && !action.isEmpty()) {
                predicates.add(cb.like(root.get("action"), "%" + action + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return automationRuleRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRuleStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRules", automationRuleRepository.count());
        stats.put("activeRules", automationRuleRepository.findByIsActiveTrue().size());
        return stats;
    }

    private AutomationRuleResponse toResponse(AutomationRule rule) {
        return AutomationRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .trigger(rule.getTrigger())
                .action(rule.getAction())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
