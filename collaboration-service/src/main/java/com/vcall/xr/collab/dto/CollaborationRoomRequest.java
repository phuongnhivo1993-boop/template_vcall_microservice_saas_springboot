package com.vcall.xr.collab.dto;

import com.vcall.xr.collab.domain.CollaborationRoom.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CollaborationRoomRequest {

    @NotNull(message = "tenantId is required")
    private UUID tenantId;

    private UUID sceneId;

    @NotBlank(message = "name is required")
    private String name;

    private Integer maxParticipants;

    @NotNull(message = "hostUserId is required")
    private UUID hostUserId;
}
