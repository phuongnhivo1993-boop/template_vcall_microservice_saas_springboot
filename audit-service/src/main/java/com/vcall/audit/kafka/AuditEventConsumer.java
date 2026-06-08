package com.vcall.audit.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.audit.entity.AuditLog;
import com.vcall.audit.entity.SecurityLog;
import com.vcall.audit.service.AuditLogService;
import com.vcall.audit.service.FraudDetectionService;
import com.vcall.audit.service.SecurityLogService;
import com.vcall.common.kafka.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditLogService auditLogService;
    private final SecurityLogService securityLogService;
    private final FraudDetectionService fraudDetectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topicPattern = "${audit.kafka.topic-pattern:.*}")
    public void consumeEvent(@Payload String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            log.info("Consumed Kafka event: type={}, topic={}, id={}", event.getType(), event.getTopic(), event.getId());

            String actorIdStr = event.getKey();
            UUID actorId = actorIdStr != null ? parseUuidSafe(actorIdStr) : UUID.randomUUID();
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                    .actorId(actorId)
                    .actorType(determineActorType(event))
                    .action(determineAction(event))
                    .resource(event.getTopic())
                    .resourceId(event.getKey())
                    .resourceType(event.getType())
                    .details(event.getPayload())
                    .correlationId(event.getId())
                    .status(AuditLog.AuditStatus.SUCCESS)
                    .build();
            auditLogService.createLog(auditLog);

            if (isSecurityEvent(event)) {
                SecurityLog securityLog = SecurityLog.builder()
                        .timestamp(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                        .eventType(mapToSecurityEventType(event))
                        .username(event.getKey())
                        .details(event.getPayload())
                        .riskLevel(SecurityLog.RiskLevel.LOW)
                        .isSuspicious(false)
                        .build();
                securityLogService.createLog(securityLog);
            }

            fraudDetectionService.detectFraud(message);

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize Kafka event: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing Kafka event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "security-events")
    public void consumeSecurityEvent(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            log.info("Consumed security event: type={}", event.getType());

            SecurityLog securityLog = SecurityLog.builder()
                    .timestamp(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                    .eventType(mapToSecurityEventType(event))
                    .username(event.getKey())
                    .details(event.getPayload())
                    .riskLevel(SecurityLog.RiskLevel.MEDIUM)
                    .isSuspicious(false)
                    .build();
            securityLogService.createLog(securityLog);

            fraudDetectionService.detectFraud(message);

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize security event: {}", e.getMessage());
        }
    }

    private AuditLog.ActorType determineActorType(KafkaEvent event) {
        if (event.getSource() == null) return AuditLog.ActorType.SYSTEM;
        return switch (event.getSource().toLowerCase()) {
            case "user" -> AuditLog.ActorType.USER;
            case "api" -> AuditLog.ActorType.API;
            case "integration" -> AuditLog.ActorType.INTEGRATION;
            default -> AuditLog.ActorType.SYSTEM;
        };
    }

    private AuditLog.Action determineAction(KafkaEvent event) {
        if (event.getType() == null) return AuditLog.Action.READ;
        return switch (event.getType().toUpperCase()) {
            case "CREATE", "CREATED" -> AuditLog.Action.CREATE;
            case "UPDATE", "UPDATED" -> AuditLog.Action.UPDATE;
            case "DELETE", "DELETED" -> AuditLog.Action.DELETE;
            case "LOGIN" -> AuditLog.Action.LOGIN;
            case "LOGOUT" -> AuditLog.Action.LOGOUT;
            case "EXPORT" -> AuditLog.Action.EXPORT;
            case "IMPORT" -> AuditLog.Action.IMPORT;
            default -> AuditLog.Action.READ;
        };
    }

    private boolean isSecurityEvent(KafkaEvent event) {
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        return type.contains("LOGIN") || type.contains("LOGOUT") || type.contains("PASSWORD")
                || type.contains("TOKEN") || type.contains("MFA") || type.contains("ACCESS");
    }

    private UUID parseUuidSafe(String str) {
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            return UUID.randomUUID();
        }
    }

    private SecurityLog.EventType mapToSecurityEventType(KafkaEvent event) {
        if (event.getType() == null) return SecurityLog.EventType.ACCESS_DENIED;
        return switch (event.getType().toUpperCase()) {
            case "LOGIN_SUCCESS" -> SecurityLog.EventType.LOGIN_SUCCESS;
            case "LOGIN_FAILED" -> SecurityLog.EventType.LOGIN_FAILED;
            case "LOGOUT" -> SecurityLog.EventType.LOGOUT;
            case "TOKEN_REFRESH" -> SecurityLog.EventType.TOKEN_REFRESH;
            case "PASSWORD_CHANGE" -> SecurityLog.EventType.PASSWORD_CHANGE;
            case "ACCESS_DENIED" -> SecurityLog.EventType.ACCESS_DENIED;
            case "UNAUTHORIZED_ACCESS" -> SecurityLog.EventType.UNAUTHORIZED_ACCESS;
            case "ACCOUNT_LOCKED" -> SecurityLog.EventType.ACCOUNT_LOCKED;
            case "MFA_CHALLENGE" -> SecurityLog.EventType.MFA_CHALLENGE;
            default -> SecurityLog.EventType.ACCESS_DENIED;
        };
    }
}
