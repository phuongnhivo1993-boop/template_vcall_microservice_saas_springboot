package com.vcall.call.controller;

import com.vcall.call.dto.RoutingRuleRequest;
import com.vcall.call.dto.RoutingRuleResponse;
import com.vcall.call.entity.CallRoutingRule;
import com.vcall.call.repository.CallRoutingRuleRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/routing-rules")
@RequiredArgsConstructor
public class RoutingRuleController {

    private final CallRoutingRuleRepository routingRuleRepository;

    @PostMapping
    public ResponseEntity<RoutingRuleResponse> createRule(@Valid @RequestBody RoutingRuleRequest request) {
        CallRoutingRule rule = new CallRoutingRule();
        rule.setName(request.getName());
        rule.setPriority(request.getPriority());
        rule.setCondition(request.getCondition());
        rule.setDestination(request.getDestination());
        rule.setDestinationId(request.getDestinationId());
        rule.setTimeProfile(request.getTimeProfile());
        rule.setActive(request.isActive());
        rule = routingRuleRepository.save(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(rule));
    }

    @GetMapping
    public ResponseEntity<List<RoutingRuleResponse>> getAllRules() {
        return ResponseEntity.ok(routingRuleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutingRuleResponse> getRule(@PathVariable Long id) {
        CallRoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));
        return ResponseEntity.ok(toResponse(rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutingRuleResponse> updateRule(@PathVariable Long id,
                                                           @Valid @RequestBody RoutingRuleRequest request) {
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
        return ResponseEntity.ok(toResponse(rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        CallRoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));
        routingRuleRepository.delete(rule);
        return ResponseEntity.noContent().build();
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
