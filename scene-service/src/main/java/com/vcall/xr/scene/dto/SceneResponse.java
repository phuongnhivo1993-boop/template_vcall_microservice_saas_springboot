package com.vcall.xr.scene.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private String type;
    private String thumbnailUrl;
    private String backgroundType;
    private UUID backgroundAssetId;
    private String status;
    private String publishedUrl;
    private LocalDateTime publishedAt;
    private Integer version;
    private String settings;
    private Integer viewCount;
    private Double avgViewTimeSeconds;
    private Long nodeCount;
    private Long hotspotCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
