package com.vcall.xr.collab.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
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
public class CollaborationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, UUID> sessionRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("Collaboration WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            String roomId = (String) payload.get("roomId");

            if ("JOIN".equals(type) && roomId != null) {
                sessionRoomMap.put(session.getId(), UUID.fromString(roomId));
                log.info("Session {} joined room {}", session.getId(), roomId);
            }

            broadcastToRoom(UUID.fromString(roomId), type, message.getPayload());
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            session.sendMessage(new TextMessage("{\"error\":\"" + e.getMessage() + "\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        UUID roomId = sessionRoomMap.remove(session.getId());
        if (roomId != null) {
            log.info("Session {} left room {}", session.getId(), roomId);
        }
        log.info("Collaboration WebSocket connection closed: {}", session.getId());
    }

    public void broadcastToRoom(UUID roomId, String type, String data) {
        String roomStr = roomId.toString();
        sessions.values().stream()
                .filter(s -> roomStr.equals(sessionRoomMap.get(s.getId())))
                .forEach(s -> {
                    try {
                        if (s.isOpen()) {
                            Map<String, Object> message = Map.of("type", type, "data", data);
                            s.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                        }
                    } catch (IOException e) {
                        log.error("Error broadcasting message to session {}", s.getId(), e);
                    }
                });
    }

    public void broadcastToRoom(UUID roomId, String type, String excludeSessionId, String data) {
        String roomStr = roomId.toString();
        sessions.values().stream()
                .filter(s -> roomStr.equals(sessionRoomMap.get(s.getId())))
                .filter(s -> !s.getId().equals(excludeSessionId))
                .forEach(s -> {
                    try {
                        if (s.isOpen()) {
                            Map<String, Object> message = Map.of("type", type, "data", data);
                            s.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                        }
                    } catch (IOException e) {
                        log.error("Error broadcasting message to session {}", s.getId(), e);
                    }
                });
    }
}
