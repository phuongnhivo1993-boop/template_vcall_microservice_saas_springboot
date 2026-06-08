package com.vcall.xr.collab.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CollaborationParticipantResponse {

    private UUID id;
    private UUID roomId;
    private UUID userId;
    private String avatarConfig;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Boolean isMuted;
    private Boolean isScreenSharing;
}
