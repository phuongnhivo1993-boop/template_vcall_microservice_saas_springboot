package com.vcall.email.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.email.entity.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishEmailSent(EmailMessage email) {
        publishEvent("email.sent", email, "EMAIL_SENT");
    }

    public void publishEmailReceived(EmailMessage email) {
        publishEvent("email.received", email, "EMAIL_RECEIVED");
    }

    private void publishEvent(String topic, EmailMessage email, String eventType) {
        try {
            String payload = objectMapper.writeValueAsString(email);
            KafkaEvent event = KafkaEvent.create(topic, email.getMessageId(), eventType, payload);
            event.setSource(source);
            kafkaTemplate.send(topic, email.getMessageId(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize email event: {}", eventType, e);
        }
    }
}
