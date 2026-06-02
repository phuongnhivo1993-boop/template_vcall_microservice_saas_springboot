package com.vcall.chat.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.chat.entity.ChatConversation;
import com.vcall.chat.entity.ChatMessage;
import com.vcall.common.kafka.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishChatStarted(ChatConversation conversation) {
        try {
            String payload = objectMapper.writeValueAsString(conversation);
            KafkaEvent event = KafkaEvent.create("chat.started", conversation.getId().toString(),
                    "CHAT_STARTED", payload);
            event.setSource(source);
            kafkaTemplate.send("chat.started", conversation.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chat started event", e);
        }
    }

    public void publishChatUpdated(ChatConversation conversation) {
        try {
            String payload = objectMapper.writeValueAsString(conversation);
            KafkaEvent event = KafkaEvent.create("chat.updated", conversation.getId().toString(),
                    "CHAT_UPDATED", payload);
            event.setSource(source);
            kafkaTemplate.send("chat.updated", conversation.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chat updated event", e);
        }
    }

    public void publishChatDeleted(ChatConversation conversation) {
        try {
            String payload = objectMapper.writeValueAsString(conversation);
            KafkaEvent event = KafkaEvent.create("chat.deleted", conversation.getId().toString(),
                    "CHAT_DELETED", payload);
            event.setSource(source);
            kafkaTemplate.send("chat.deleted", conversation.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chat deleted event", e);
        }
    }

    public void publishChatClosed(ChatConversation conversation) {
        try {
            String payload = objectMapper.writeValueAsString(conversation);
            KafkaEvent event = KafkaEvent.create("chat.closed", conversation.getId().toString(),
                    "CHAT_CLOSED", payload);
            event.setSource(source);
            kafkaTemplate.send("chat.closed", conversation.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chat closed event", e);
        }
    }

    public void publishChatMessageSent(ChatConversation conversation, ChatMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            KafkaEvent event = KafkaEvent.create("chat.message.sent", conversation.getId().toString(),
                    "CHAT_MESSAGE_SENT", payload);
            event.setSource(source);
            kafkaTemplate.send("chat.message.sent", conversation.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chat message sent event", e);
        }
    }
}
