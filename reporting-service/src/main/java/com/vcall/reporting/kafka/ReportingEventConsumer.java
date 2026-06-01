package com.vcall.reporting.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.reporting.entity.AgentPerformanceCache;
import com.vcall.reporting.service.AgentPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportingEventConsumer {

    private final AgentPerformanceService agentPerformanceService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "call.started", groupId = "reporting-group")
    public void handleCallStarted(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
            log.info("Received call.started event: {}", event.getId());
        } catch (Exception e) {
            log.error("Error processing call.started event", e);
        }
    }

    @KafkaListener(topics = "call.ended", groupId = "reporting-group")
    public void handleCallEnded(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
            log.info("Received call.ended event: {}", event.getId());

            UUID agentId = UUID.fromString((String) payload.get("agentId"));
            String agentName = (String) payload.getOrDefault("agentName", "Unknown");
            long duration = payload.get("duration") != null ? ((Number) payload.get("duration")).longValue() : 0L;
            String status = (String) payload.getOrDefault("status", "completed");

            LocalDate today = LocalDate.now();
            long answered = "answered".equals(status) ? 1 : 0;
            long missed = "missed".equals(status) ? 1 : 0;

            agentPerformanceService.updatePerformance(
                    agentId, agentName, AgentPerformanceCache.Period.DAILY, today,
                    1L, answered, missed, (double) duration, 0.0,
                    duration, 0, 0.0, 0.0
            );
        } catch (Exception e) {
            log.error("Error processing call.ended event", e);
        }
    }

    @KafkaListener(topics = "ticket.closed", groupId = "reporting-group")
    public void handleTicketClosed(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            log.info("Received ticket.closed event: {}", event.getId());
        } catch (Exception e) {
            log.error("Error processing ticket.closed event", e);
        }
    }

    @KafkaListener(topics = "cdr.generated", groupId = "reporting-group")
    public void handleCdrGenerated(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            log.info("Received cdr.generated event: {}", event.getId());
        } catch (Exception e) {
            log.error("Error processing cdr.generated event", e);
        }
    }
}
