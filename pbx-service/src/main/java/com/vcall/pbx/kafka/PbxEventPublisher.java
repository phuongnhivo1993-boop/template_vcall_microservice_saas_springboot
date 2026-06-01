package com.vcall.pbx.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.pbx.entity.Extension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PbxEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishExtensionCreated(Extension extension) {
        try {
            String payload = objectMapper.writeValueAsString(extension);
            KafkaEvent event = KafkaEvent.create("pbx.extension.created", extension.getId().toString(), "EXTENSION_CREATED", payload);
            event.setSource(source);
            kafkaTemplate.send("pbx.extension.created", extension.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize extension created event", e);
        }
    }

    public void publishExtensionStatusChanged(Extension extension) {
        try {
            String payload = objectMapper.writeValueAsString(extension);
            KafkaEvent event = KafkaEvent.create("pbx.extension.status.changed", extension.getId().toString(), "EXTENSION_STATUS_CHANGED", payload);
            event.setSource(source);
            kafkaTemplate.send("pbx.extension.status.changed", extension.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize extension status changed event", e);
        }
    }
}
