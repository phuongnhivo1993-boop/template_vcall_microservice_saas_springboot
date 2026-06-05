package com.vcall.xr.scene.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SceneEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_SCENE = "xr.scene.events";

    public SceneEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSceneCreated(com.vcall.xr.scene.domain.Scene scene) {
        publishEvent("SCENE_CREATED", scene);
    }

    public void publishSceneUpdated(com.vcall.xr.scene.domain.Scene scene) {
        publishEvent("SCENE_UPDATED", scene);
    }

    public void publishSceneDeleted(java.util.UUID sceneId) {
        publishEvent("SCENE_DELETED", sceneId);
    }

    public void publishScenePublished(com.vcall.xr.scene.domain.Scene scene) {
        publishEvent("SCENE_PUBLISHED", scene);
    }

    private void publishEvent(String eventType, Object payload) {
        try {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("eventType", eventType);
            event.put("payload", payload);
            event.put("timestamp", java.time.LocalDateTime.now());
            kafkaTemplate.send(TOPIC_SCENE, eventType, event);
            log.debug("Published event: {} for {}", eventType, payload);
        } catch (Exception e) {
            log.error("Failed to publish event {}: {}", eventType, e.getMessage(), e);
        }
    }
}
