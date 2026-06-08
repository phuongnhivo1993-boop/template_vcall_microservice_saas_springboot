package com.vcall.xr.collab.dto;

import com.vcall.xr.collab.domain.CollaborationRoom;

public class CollaborationRoomMapper {

    public static CollaborationRoomResponse toResponse(CollaborationRoom entity) {
        if (entity == null) return null;
        return CollaborationRoomResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .sceneId(entity.getSceneId())
                .name(entity.getName())
                .maxParticipants(entity.getMaxParticipants())
                .status(entity.getStatus())
                .hostUserId(entity.getHostUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public static CollaborationRoom toEntity(CollaborationRoomRequest request) {
        if (request == null) return null;
        CollaborationRoom room = new CollaborationRoom();
        room.setTenantId(request.getTenantId());
        room.setSceneId(request.getSceneId());
        room.setName(request.getName());
        room.setMaxParticipants(request.getMaxParticipants());
        room.setHostUserId(request.getHostUserId());
        return room;
    }
}
