package com.vcall.xr.twin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DigitalTwinRequest {

    @NotNull(message = "tenantId is required")
    private UUID tenantId;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "type is required")
    private String type;

    private UUID bimAssetId;

    private UUID sceneId;

    private String iotEndpoints;

    private Integer syncIntervalSeconds;

    private String floors;

    private String rooms;
}
