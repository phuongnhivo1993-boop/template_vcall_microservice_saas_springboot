package com.vcall.cdr.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.cdr.entity.CdrRecord;
import com.vcall.common.kafka.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CdrEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishCdrGenerated(CdrRecord record) {
        try {
            String payload = objectMapper.writeValueAsString(record);
            KafkaEvent event = KafkaEvent.create("cdr.generated", record.getCallId(), "CDR_GENERATED", payload);
            event.setSource(source);
            kafkaTemplate.send("cdr.generated", record.getCallId(), event);
            log.info("Published cdr.generated event for callId: {}", record.getCallId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize CDR event for callId: {}", record.getCallId(), e);
        }
    }
}
