package com.vcall.sipservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.sipservice.entity.SipAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SipEventPublisher {

    private final KafkaTemplate<String, KafkaEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:sip-service}")
    private String source;

    public void publishAccountRegistered(SipAccount account) {
        try {
            String payload = objectMapper.writeValueAsString(account);
            KafkaEvent event = KafkaEvent.create(
                    "sip.account.registered",
                    account.getId().toString(),
                    "SIP_ACCOUNT_REGISTERED",
                    payload
            );
            event.setSource(source);
            kafkaTemplate.send("sip.account.registered", event.getKey(), event);
            log.info("Published sip.account.registered event for account {}", account.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SipAccount for Kafka event", e);
        }
    }

    public void publishAccountUnregistered(SipAccount account) {
        try {
            String payload = objectMapper.writeValueAsString(account);
            KafkaEvent event = KafkaEvent.create(
                    "sip.account.unregistered",
                    account.getId().toString(),
                    "SIP_ACCOUNT_UNREGISTERED",
                    payload
            );
            event.setSource(source);
            kafkaTemplate.send("sip.account.unregistered", event.getKey(), event);
            log.info("Published sip.account.unregistered event for account {}", account.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SipAccount for Kafka event", e);
        }
    }
}
