package com.vcall.iam.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.dto.UserEventDTO;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.iam.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, KafkaEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishUserCreated(User user) {
        publishEvent("user.created", user);
    }

    public void publishUserUpdated(User user) {
        publishEvent("user.updated", user);
    }

    private void publishEvent(String eventType, User user) {
        try {
            UserEventDTO dto = UserEventDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .status(user.getStatus() != null ? user.getStatus().name() : null)
                    .roles(user.getUserRoles().stream()
                            .map(ur -> ur.getRole().getName().name())
                            .collect(Collectors.toSet()))
                    .build();
            String payload = objectMapper.writeValueAsString(dto);
            KafkaEvent event = KafkaEvent.create("iam-user-events", user.getId().toString(), eventType, payload);
            event.setSource("iam-service");
            kafkaTemplate.send("iam-user-events", user.getId().toString(), event);
            log.info("Published {} event for user: {}", eventType, user.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user for event {}: {}", eventType, e.getMessage());
        }
    }
}
