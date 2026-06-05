package com.vcall.xr.scene.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotRequest {

    private UUID nodeId;

    @NotBlank(message = "Hotspot type is required")
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
}
