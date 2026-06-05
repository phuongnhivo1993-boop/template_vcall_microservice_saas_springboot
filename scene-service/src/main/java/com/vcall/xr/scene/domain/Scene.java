package com.vcall.xr.scene.domain;

import com.vcall.common.entity.BaseEntity;
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
@Table(name = "xr_scene")
@SQLRestriction("is_deleted = false")
public class Scene extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SceneType type;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "background_type", length = 50)
    private String backgroundType;

    @Column(name = "background_asset_id")
    private UUID backgroundAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PublishStatus status;

    @Column(name = "published_url", length = 500)
    private String publishedUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "version")
    private Integer version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", columnDefinition = "jsonb")
    private String settings;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "avg_view_time_seconds")
    private Double avgViewTimeSeconds;
}
