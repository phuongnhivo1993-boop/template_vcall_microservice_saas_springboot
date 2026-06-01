package com.vcall.recording.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.recording.entity.Recording;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecordingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishCallRecorded(Recording recording) {
        try {
            String payload = objectMapper.writeValueAsString(recording);
            KafkaEvent event = KafkaEvent.create("call.recorded", recording.getCallId().toString(), "CALL_RECORDED", payload);
            event.setSource(source);
            kafkaTemplate.send("call.recorded", recording.getCallId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize call recorded event", e);
        }
    }

    public void publishRecordingCompleted(Recording recording) {
        try {
            String payload = objectMapper.writeValueAsString(recording);
            KafkaEvent event = KafkaEvent.create("recording.completed", recording.getId().toString(), "RECORDING_COMPLETED", payload);
            event.setSource(source);
            kafkaTemplate.send("recording.completed", recording.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize recording completed event", e);
        }
    }

    public void publishRecordingDeleted(Recording recording) {
        try {
            String payload = objectMapper.writeValueAsString(recording);
            KafkaEvent event = KafkaEvent.create("recording.deleted", recording.getId().toString(), "RECORDING_DELETED", payload);
            event.setSource(source);
            kafkaTemplate.send("recording.deleted", recording.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize recording deleted event", e);
        }
    }
}
