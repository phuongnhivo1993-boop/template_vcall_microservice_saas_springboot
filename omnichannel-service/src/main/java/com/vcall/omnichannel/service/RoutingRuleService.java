package com.vcall.omnichannel.service;

import com.vcall.omnichannel.dto.request.RoutingRuleRequest;
import com.vcall.omnichannel.dto.response.RoutingRuleResponse;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.OmnichannelRoutingRule;
import com.vcall.omnichannel.repository.OmnichannelRoutingRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutingRuleService {

    private final OmnichannelRoutingRuleRepository routingRuleRepository;

    @Transactional(readOnly = true)
    public List<RoutingRuleResponse> getAll(Channel channel) {
        List<OmnichannelRoutingRule> rules;
        if (channel != null) {
            rules = routingRuleRepository.findByChannelAndIsActiveTrueOrderByPriority(channel);
        } else {
            rules = routingRuleRepository.findAll();
        }
        return rules.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoutingRuleResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public RoutingRuleResponse create(RoutingRuleRequest request) {
        OmnichannelRoutingRule rule = new OmnichannelRoutingRule();
        rule.setName(request.getName());
        rule.setChannel(request.getChannel());
        rule.setCondition(request.getCondition());
        rule.setPriority(request.getPriority());
        rule.setDestinationType(request.getDestinationType());
        rule.setDestinationId(request.getDestinationId());
        rule.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        rule = routingRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public RoutingRuleResponse update(Long id, RoutingRuleRequest request) {
        OmnichannelRoutingRule rule = findById(id);
        rule.setName(request.getName());
        rule.setChannel(request.getChannel());
        rule.setCondition(request.getCondition());
        rule.setPriority(request.getPriority());
        rule.setDestinationType(request.getDestinationType());
        rule.setDestinationId(request.getDestinationId());
        rule.setIsActive(request.getIsActive());

        rule = routingRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public void delete(Long id) {
        OmnichannelRoutingRule rule = findById(id);
        routingRuleRepository.delete(rule);
    }

    private OmnichannelRoutingRule findById(Long id) {
        return routingRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RoutingRule not found with id: " + id));
    }

    private RoutingRuleResponse toResponse(OmnichannelRoutingRule rule) {
        return RoutingRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .channel(rule.getChannel())
                .condition(rule.getCondition())
                .priority(rule.getPriority())
                .destinationType(rule.getDestinationType())
                .destinationId(rule.getDestinationId())
                .isActive(rule.getIsActive())
                .build();
    }
}
