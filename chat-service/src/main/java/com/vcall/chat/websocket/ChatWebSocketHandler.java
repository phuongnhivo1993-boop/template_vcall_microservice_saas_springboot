package com.vcall.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.chat.dto.ChatMessageRequest;
import com.vcall.chat.dto.ChatMessageResponse;
import com.vcall.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            ChatMessageRequest request = objectMapper.readValue(message.getPayload(), ChatMessageRequest.class);
            String conversationId = (String) session.getAttributes().get("conversationId");
            if (conversationId == null) {
                session.sendMessage(new TextMessage("{\"error\":\"No conversation ID in session\"}"));
                return;
            }
            ChatMessageResponse response = chatMessageService.sendMessage(
                    UUID.fromString(conversationId), request);
            String responseJson = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(responseJson));
            broadcastToConversation(conversationId, responseJson, session.getId());
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            session.sendMessage(new TextMessage("{\"error\":\"" + e.getMessage() + "\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket connection closed: {}", session.getId());
    }

    private void broadcastToConversation(String conversationId, String message, String excludeSessionId) {
        sessions.values().stream()
                .filter(s -> !s.getId().equals(excludeSessionId))
                .filter(s -> conversationId.equals(s.getAttributes().get("conversationId")))
                .forEach(s -> {
                    try {
                        if (s.isOpen()) {
                            s.sendMessage(new TextMessage(message));
                        }
                    } catch (IOException e) {
                        log.error("Error broadcasting message", e);
                    }
                });
    }
}
