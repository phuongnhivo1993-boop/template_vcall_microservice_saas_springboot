package com.vcall.agent.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.agent.dto.AgentStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentStatusWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String AGENT_STATUS_TOPIC = "/topic/agent-status";

    public void broadcastStatusChange(AgentStatusResponse statusResponse) {
        try {
            String payload = objectMapper.writeValueAsString(statusResponse);
            messagingTemplate.convertAndSend(AGENT_STATUS_TOPIC, payload);
            log.debug("Broadcast agent status change to {}: {}", AGENT_STATUS_TOPIC, statusResponse.getAgentId());
        } catch (Exception e) {
            log.error("Failed to broadcast agent status change: {}", e.getMessage());
        }
    }

    public void sendStatusToAgent(String sessionId, AgentStatusResponse statusResponse) {
        try {
            String payload = objectMapper.writeValueAsString(statusResponse);
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/agent-status", payload);
        } catch (Exception e) {
            log.error("Failed to send status to agent {}: {}", sessionId, e.getMessage());
        }
    }
}
