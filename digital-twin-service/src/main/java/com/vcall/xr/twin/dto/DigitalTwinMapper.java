package com.vcall.xr.twin.dto;

import com.vcall.xr.twin.domain.DigitalTwin;

public class DigitalTwinMapper {

    public static DigitalTwinResponse toResponse(DigitalTwin entity) {
        if (entity == null) return null;
        return DigitalTwinResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .type(entity.getType())
                .bimAssetId(entity.getBimAssetId())
                .sceneId(entity.getSceneId())
                .iotEndpoints(entity.getIotEndpoints())
                .syncIntervalSeconds(entity.getSyncIntervalSeconds())
                .floors(entity.getFloors())
                .rooms(entity.getRooms())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public static DigitalTwin toEntity(DigitalTwinRequest request) {
        if (request == null) return null;
        DigitalTwin twin = new DigitalTwin();
        twin.setTenantId(request.getTenantId());
        twin.setName(request.getName());
        twin.setType(request.getType());
        twin.setBimAssetId(request.getBimAssetId());
        twin.setSceneId(request.getSceneId());
        twin.setIotEndpoints(request.getIotEndpoints());
        twin.setSyncIntervalSeconds(request.getSyncIntervalSeconds());
        twin.setFloors(request.getFloors());
        twin.setRooms(request.getRooms());
        return twin;
    }
}
