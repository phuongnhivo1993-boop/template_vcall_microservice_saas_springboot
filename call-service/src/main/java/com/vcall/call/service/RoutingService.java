package com.vcall.call.service;

import com.vcall.call.dto.CallRequest;
import com.vcall.call.dto.CallResponse;
import com.vcall.call.entity.CallRoutingRule;
import com.vcall.call.repository.CallRoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private boolean matchesCondition(CallRoutingRule rule, CallRequest request) {
        if (rule.getCondition() == null || rule.getCondition().isBlank()) {
            return true;
        }
        return true;
    }
}
