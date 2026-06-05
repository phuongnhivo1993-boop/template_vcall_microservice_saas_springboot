package com.vcall.xr.scene.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "xr_hotspot")
@SQLRestriction("is_deleted = false")
public class Hotspot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "scene_id", nullable = false)
    private UUID sceneId;

    @Column(name = "node_id")
    private UUID nodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "hotspot_type", nullable = false)
    private HotspotType hotspotType;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_payload", columnDefinition = "jsonb")
    private String actionPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "style", columnDefinition = "jsonb")
    private String style;

    @Column(name = "animation", length = 100)
    private String animation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public enum HotspotType {
        INFO,
        NAVIGATION,
        MEDIA,
        LINK,
        ACTION,
        ANNOTATION
    }

    public enum ActionType {
        OPEN_URL,
        PLAY_MEDIA,
        NAVIGATE_SCENE,
        SHOW_INFO,
        TRIGGER_ANIMATION,
        OPEN_MODAL
    }
}
