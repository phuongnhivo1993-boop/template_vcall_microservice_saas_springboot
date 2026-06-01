package com.vcall.omnichannel.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.omnichannel.entity.Conversation;
import com.vcall.omnichannel.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OmnichannelEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:omnichannel-service}")
    private String source;

    public void publishConversationCreated(Conversation conversation) {
        publish("omnichannel.conversation.created", conversation.getId().toString(),
                "CONVERSATION_CREATED", conversation);
    }

    public void publishConversationAssigned(Conversation conversation) {
        publish("omnichannel.conversation.assigned", conversation.getId().toString(),
                "CONVERSATION_ASSIGNED", conversation);
    }

    public void publishConversationClosed(Conversation conversation) {
        publish("omnichannel.conversation.closed", conversation.getId().toString(),
                "CONVERSATION_CLOSED", conversation);
    }

    public void publishMessageSent(Message message) {
        publish("omnichannel.message.sent", message.getId().toString(),
                "MESSAGE_SENT", message);
    }

    private void publish(String topic, String key, String type, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            KafkaEvent event = KafkaEvent.create(topic, key, type, payloadJson);
            event.setSource(source);
            kafkaTemplate.send(topic, key, event);
            log.info("Published event type={} to topic={} key={}", type, topic, key);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload for topic: {}", topic, e);
        }
    }
}
