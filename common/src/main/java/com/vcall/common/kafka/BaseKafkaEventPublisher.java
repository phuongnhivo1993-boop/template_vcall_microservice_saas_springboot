package com.vcall.common.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public abstract class BaseKafkaEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String defaultSource;

    protected abstract String getSource();

    protected void publishEvent(String topic, Object data, String eventType, String key) {
        try {
            String payload = objectMapper.writeValueAsString(data);
            KafkaEvent event = KafkaEvent.create(topic, key, eventType, payload);
            event.setSource(getSource() != null ? getSource() : defaultSource);
            kafkaTemplate.send(topic, key, event);
            log.info("Published event: {} to topic: {}", eventType, topic);
        } catch (JsonProcessingException e) {
            log.error("Failed to publish event {} to topic {}: {}", eventType, topic, e.getMessage());
        }
    }
}
