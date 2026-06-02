package com.vcall.omnichannel.service;

import com.vcall.omnichannel.dto.request.RoutingRuleRequest;
import com.vcall.omnichannel.dto.response.RoutingRuleResponse;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.OmnichannelRoutingRule;
import com.vcall.omnichannel.repository.OmnichannelRoutingRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutingRuleService {

    private final OmnichannelRoutingRuleRepository routingRuleRepository;

    @Transactional(readOnly = true)
    public Page<RoutingRuleResponse> getAll(Channel channel, Pageable pageable) {
        if (channel != null) {
            return routingRuleRepository.findByChannelAndIsActiveTrueOrderByPriority(channel, pageable)
                    .map(this::toResponse);
        }
        return routingRuleRepository.findAll(pageable).map(this::toResponse);
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
        rule.setIsDeleted(true);
        routingRuleRepository.save(rule);
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
