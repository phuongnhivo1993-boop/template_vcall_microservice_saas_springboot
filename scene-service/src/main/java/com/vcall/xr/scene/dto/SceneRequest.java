package com.vcall.xr.scene.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneRequest {

    @NotBlank(message = "Scene name is required")
    private String name;

    private String description;

    @NotNull(message = "Scene type is required")
    private String type;

    private UUID tenantId;

    private String thumbnailUrl;

    private String backgroundType;

    private UUID backgroundAssetId;

    private String settings;
}
