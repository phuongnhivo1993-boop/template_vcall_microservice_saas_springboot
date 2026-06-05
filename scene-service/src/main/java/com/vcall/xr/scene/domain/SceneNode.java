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
@Table(name = "xr_scene_node")
@SQLRestriction("is_deleted = false")
public class SceneNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "scene_id", nullable = false)
    private UUID sceneId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    private NodeType nodeType;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "position_x")
    private Float positionX;

    @Column(name = "position_y")
    private Float positionY;

    @Column(name = "position_z")
    private Float positionZ;

    @Column(name = "rotation_x")
    private Float rotationX;

    @Column(name = "rotation_y")
    private Float rotationY;

    @Column(name = "rotation_z")
    private Float rotationZ;

    @Column(name = "scale_x")
    private Float scaleX;

    @Column(name = "scale_y")
    private Float scaleY;

    @Column(name = "scale_z")
    private Float scaleZ;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb")
    private String content;

    @Column(name = "visible")
    private Boolean visible;

    @Column(name = "interactive")
    private Boolean interactive;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public enum NodeType {
        ROOT,
        GROUP,
        MODEL_3D,
        IMAGE,
        VIDEO,
        AUDIO,
        TEXT,
        LIGHT,
        CAMERA,
        ANNOTATION,
        UI_ELEMENT,
        ENVIRONMENT
    }
}
