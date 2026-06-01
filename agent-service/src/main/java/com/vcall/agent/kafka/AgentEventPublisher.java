package com.vcall.agent.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.agent.entity.Agent;
import com.vcall.agent.entity.AgentSession;
import com.vcall.common.kafka.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishAgentCreated(Agent agent) {
        try {
            String payload = objectMapper.writeValueAsString(agent);
            KafkaEvent event = KafkaEvent.create("agent.created", agent.getId().toString(), "AGENT_CREATED", payload);
            event.setSource(source);
            kafkaTemplate.send("agent.created", agent.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize agent created event", e);
        }
    }

    public void publishStatusChanged(Agent agent, String reason) {
        try {
            String payload = objectMapper.writeValueAsString(agent);
            KafkaEvent event = KafkaEvent.create("agent.status.changed", agent.getId().toString(), "AGENT_STATUS_CHANGED", payload);
            event.setSource(source);
            kafkaTemplate.send("agent.status.changed", agent.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize status changed event", e);
        }
    }

    public void publishSessionStarted(AgentSession session) {
        try {
            String payload = objectMapper.writeValueAsString(session);
            KafkaEvent event = KafkaEvent.create("agent.session.started", session.getAgent().getId().toString(), "AGENT_SESSION_STARTED", payload);
            event.setSource(source);
            kafkaTemplate.send("agent.session.started", session.getAgent().getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize session started event", e);
        }
    }

    public void publishSessionEnded(AgentSession session) {
        try {
            String payload = objectMapper.writeValueAsString(session);
            KafkaEvent event = KafkaEvent.create("agent.session.ended", session.getAgent().getId().toString(), "AGENT_SESSION_ENDED", payload);
            event.setSource(source);
            kafkaTemplate.send("agent.session.ended", session.getAgent().getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize session ended event", e);
        }
    }
}
