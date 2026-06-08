package com.vcall.xr.webxr.dto;

import com.vcall.xr.webxr.domain.DeviceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class XrSessionResponse {

    private UUID id;
    private UUID userId;
    private String tenantId;
    private UUID sceneId;
    private DeviceType deviceType;
    private String deviceInfo;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;
    private String gazeData;
    private String interactions;
    private Double fpsAvg;
    private Long loadTimeMs;
}
