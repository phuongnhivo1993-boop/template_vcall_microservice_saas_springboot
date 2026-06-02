package com.vcall.call.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.call.entity.Call;
import com.vcall.common.kafka.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishCallStarted(Call call) {
        publishEvent("call.started", call, "CALL_STARTED");
    }

    public void publishCallAnswered(Call call) {
        publishEvent("call.answered", call, "CALL_ANSWERED");
    }

    public void publishCallEnded(Call call) {
        publishEvent("call.ended", call, "CALL_ENDED");
    }

    public void publishCallQueueJoined(Call call) {
        publishEvent("call.queue.joined", call, "CALL_QUEUE_JOINED");
    }

    public void publishCallQueueLeft(Call call) {
        publishEvent("call.queue.left", call, "CALL_QUEUE_LEFT");
    }

    public void publishEvent(String eventType, Call call) {
        publishEvent("call.events", call, eventType);
    }

    private void publishEvent(String topic, Call call, String eventType) {
        try {
            String payload = objectMapper.writeValueAsString(call);
            KafkaEvent event = KafkaEvent.create(topic, call.getCallId(), eventType, payload);
            event.setSource(source);
            kafkaTemplate.send(topic, call.getCallId(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize call event: {}", eventType, e);
        }
    }
}
