package com.vcall.crm.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrmEventPublisher {

    private static final String TOPIC_CRM = "crm.events";

    private final KafkaTemplate<String, KafkaEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishLeadCreated(Object lead) {
        try {
            String payload = objectMapper.writeValueAsString(lead);
            KafkaEvent event = KafkaEvent.create(TOPIC_CRM, "lead", "lead.created", payload);
            kafkaTemplate.send(TOPIC_CRM, event.getKey(), event);
            log.info("Published lead.created event for lead: {}", lead);
        } catch (Exception e) {
            log.error("Failed to publish lead.created event", e);
        }
    }

    public void publishLeadConverted(Object lead, Object opportunity) {
        try {
            String payload = objectMapper.writeValueAsString(
                    java.util.Map.of("lead", lead, "opportunity", opportunity)
            );
            KafkaEvent event = KafkaEvent.create(TOPIC_CRM, "lead", "lead.converted", payload);
            kafkaTemplate.send(TOPIC_CRM, event.getKey(), event);
            log.info("Published lead.converted event");
        } catch (Exception e) {
            log.error("Failed to publish lead.converted event", e);
        }
    }

    public void publishOpportunityStageChanged(Object opportunity) {
        try {
            String payload = objectMapper.writeValueAsString(opportunity);
            KafkaEvent event = KafkaEvent.create(TOPIC_CRM, "opportunity", "opportunity.stage.changed", payload);
            kafkaTemplate.send(TOPIC_CRM, event.getKey(), event);
            log.info("Published opportunity.stage.changed event");
        } catch (Exception e) {
            log.error("Failed to publish opportunity.stage.changed event", e);
        }
    }
}
