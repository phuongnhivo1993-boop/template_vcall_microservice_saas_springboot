package com.vcall.ticket.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.ticket.entity.SlaBreach;
import com.vcall.ticket.entity.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishTicketCreated(Ticket ticket) {
        try {
            String payload = objectMapper.writeValueAsString(ticket);
            KafkaEvent event = KafkaEvent.create("ticket.created", ticket.getId().toString(), "TICKET_CREATED", payload);
            event.setSource(source);
            kafkaTemplate.send("ticket.created", ticket.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ticket created event", e);
        }
    }

    public void publishTicketClosed(Ticket ticket) {
        try {
            String payload = objectMapper.writeValueAsString(ticket);
            KafkaEvent event = KafkaEvent.create("ticket.closed", ticket.getId().toString(), "TICKET_CLOSED", payload);
            event.setSource(source);
            kafkaTemplate.send("ticket.closed", ticket.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ticket closed event", e);
        }
    }

    public void publishTicketEscalated(Ticket ticket, String reason) {
        try {
            String payload = objectMapper.writeValueAsString(ticket);
            KafkaEvent event = KafkaEvent.create("ticket.escalated", ticket.getId().toString(), "TICKET_ESCALATED", payload);
            event.setSource(source);
            event.setKey(reason);
            kafkaTemplate.send("ticket.escalated", ticket.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ticket escalated event", e);
        }
    }

    public void publishSlaBreach(SlaBreach breach) {
        try {
            String payload = objectMapper.writeValueAsString(breach);
            KafkaEvent event = KafkaEvent.create("sla.breach", breach.getTicket().getId().toString(), "SLA_BREACH", payload);
            event.setSource(source);
            kafkaTemplate.send("sla.breach", breach.getTicket().getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SLA breach event", e);
        }
    }
}
