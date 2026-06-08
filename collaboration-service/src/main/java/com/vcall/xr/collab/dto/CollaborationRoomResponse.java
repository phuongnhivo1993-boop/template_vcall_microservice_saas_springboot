package com.vcall.xr.collab.dto;

import com.vcall.xr.collab.domain.CollaborationRoom.RoomStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CollaborationRoomResponse {

    private UUID id;
    private UUID tenantId;
    private UUID sceneId;
    private String name;
    private Integer maxParticipants;
    private RoomStatus status;
    private UUID hostUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}
