package com.vcall.xr.twin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class DigitalTwinResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String type;
    private UUID bimAssetId;
    private UUID sceneId;
    private String iotEndpoints;
    private Integer syncIntervalSeconds;
    private String floors;
    private String rooms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}
