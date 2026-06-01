package com.vcall.notification.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationType;
import com.vcall.notification.service.NotificationService;
import com.vcall.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {
            "sla.breach",
            "ticket.assigned",
            "call.missed",
            "invoice.created",
            "subscription.expiring"
    })
    public void consume(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            log.info("Received event: type={}, key={}", event.getType(), event.getKey());

            NotificationType type = mapEventType(event.getType());
            if (type == null) {
                log.warn("Unknown event type: {}", event.getType());
                return;
            }

            Map<String, String> payload = parsePayload(event.getPayload());
            UUID recipientId = UUID.fromString(event.getKey());
            String recipientAddress = payload.getOrDefault("email", payload.getOrDefault("phone", ""));
            String title = payload.getOrDefault("title", type.name());
            String body = payload.getOrDefault("body", "Notification: " + type.name());

            notificationService.sendNotification(
                    com.vcall.notification.dto.NotificationRequest.builder()
                            .recipientId(recipientId)
                            .recipientAddress(recipientAddress)
                            .channel(NotificationChannel.EMAIL)
                            .type(type)
                            .title(title)
                            .body(body)
                            .metadata(event.getPayload())
                            .build()
            );
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }

    private NotificationType mapEventType(String eventType) {
        return switch (eventType) {
            case "SLA_BREACH" -> NotificationType.SLA_BREACH;
            case "TICKET_ASSIGNED" -> NotificationType.TICKET_ASSIGNED;
            case "CALL_MISSED" -> NotificationType.CALL_MISSED;
            case "INVOICE_CREATED" -> NotificationType.ALERT;
            case "SUBSCRIPTION_EXPIRING" -> NotificationType.REMINDER;
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse payload: {}", e.getMessage());
            return Map.of();
        }
    }
}
