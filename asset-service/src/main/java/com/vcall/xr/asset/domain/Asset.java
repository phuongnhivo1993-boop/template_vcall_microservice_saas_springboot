package com.vcall.xr.asset.domain;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "xr_asset")
public class Asset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AssetType type;

    @Column(name = "original_url", length = 1000)
    private String originalUrl;

    @Column(name = "processed_url", length = 1000)
    private String processedUrl;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "video_type", length = 50)
    private String videoType;

    @Column(name = "resolution", length = 20)
    private String resolution;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "model_format", length = 20)
    private String modelFormat;

    @Column(name = "has_draco_compression")
    private Boolean hasDracoCompression = false;

    @Column(name = "has_ktx2_textures")
    private Boolean hasKtx2Textures = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "transcode_status")
    private TranscodeStatus transcodeStatus = TranscodeStatus.PENDING;

    @Column(name = "processing_progress")
    private Integer processingProgress = 0;

    @Column(name = "hls_url", length = 1000)
    private String hlsUrl;

    @Column(name = "dash_url", length = 1000)
    private String dashUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variants", columnDefinition = "jsonb")
    private Map<String, Object> variants;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (transcodeStatus == null) {
            transcodeStatus = TranscodeStatus.PENDING;
        }
        if (processingProgress == null) {
            processingProgress = 0;
        }
        if (hasDracoCompression == null) {
            hasDracoCompression = false;
        }
        if (hasKtx2Textures == null) {
            hasKtx2Textures = false;
        }
    }
}
