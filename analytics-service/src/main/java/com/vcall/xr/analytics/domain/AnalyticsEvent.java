package com.vcall.xr.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_analytics_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "scene_id", columnDefinition = "UUID")
    private UUID sceneId;

    @Column(name = "session_id", columnDefinition = "UUID")
    private UUID sessionId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public enum EventType {
        SESSION_START,
        SESSION_END,
        GAZE_ENTER,
        GAZE_EXIT,
        GAZE_DWELL,
        INTERACTION_CLICK,
        INTERACTION_GRAB,
        INTERACTION_SWIPE,
        INTERACTION_PINCH,
        OBJECT_VIEW,
        OBJECT_FOCUS,
        SCENE_LOAD,
        SCENE_UNLOAD,
        NAVIGATION_MOVE,
        NAVIGATION_TELEPORT,
        UI_OPEN,
        UI_CLOSE,
        PERFORMANCE_FPS_DROP,
        PERFORMANCE_LOAD_SLOW
    }
}
