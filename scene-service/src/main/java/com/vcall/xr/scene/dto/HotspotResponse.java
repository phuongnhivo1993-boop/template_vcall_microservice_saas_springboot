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
public class HotspotResponse {

    private UUID id;
    private UUID sceneId;
    private UUID nodeId;
    private String hotspotType;
    private Double latitude;
    private Double longitude;
    private String title;
    private String description;
    private String iconUrl;
    private String actionType;
    private String actionPayload;
    private String style;
    private String animation;
    private LocalDateTime createdAt;
}
