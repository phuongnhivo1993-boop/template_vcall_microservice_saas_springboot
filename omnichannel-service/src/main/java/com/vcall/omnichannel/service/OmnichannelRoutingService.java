package com.vcall.omnichannel.service;

import com.vcall.omnichannel.entity.Conversation;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.OmnichannelRoutingRule;
import com.vcall.omnichannel.entity.OmnichannelRoutingRule.DestinationType;
import com.vcall.omnichannel.repository.OmnichannelRoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmnichannelRoutingService {

    private final OmnichannelRoutingRuleRepository routingRuleRepository;
    private final ConversationService conversationService;

    @Transactional(readOnly = true)
    public Optional<OmnichannelRoutingRule> findRoute(Channel channel) {
        List<OmnichannelRoutingRule> rules = routingRuleRepository.findByChannelAndIsActiveTrueOrderByPriority(channel);
        if (rules.isEmpty()) {
            log.warn("No active routing rules found for channel: {}", channel);
            return Optional.empty();
        }
        return Optional.of(rules.get(0));
    }

    @Transactional
    public void routeConversation(Conversation conversation) {
        Optional<OmnichannelRoutingRule> ruleOpt = findRoute(conversation.getChannel());

        if (ruleOpt.isEmpty()) {
            log.info("No routing rule for conversation {} on channel {}. Leaving unassigned.",
                    conversation.getId(), conversation.getChannel());
            return;
        }

        OmnichannelRoutingRule rule = ruleOpt.get();
        log.info("Routing conversation {} using rule '{}' to destination: {} {}",
                conversation.getId(), rule.getName(), rule.getDestinationType(), rule.getDestinationId());

        if (rule.getDestinationType() == DestinationType.AGENT) {
            UUID agentId = java.util.UUID.fromString(rule.getDestinationId());
            conversationService.assignAgent(conversation.getId(),
                    new com.vcall.omnichannel.dto.request.ConversationAssignRequest(agentId));
        }
    }
}
