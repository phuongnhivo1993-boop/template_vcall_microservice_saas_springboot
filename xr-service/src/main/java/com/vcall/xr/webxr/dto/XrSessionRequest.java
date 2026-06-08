package com.vcall.xr.webxr.dto;

import com.vcall.xr.webxr.domain.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class XrSessionRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotBlank(message = "tenantId is required")
    private String tenantId;

    @NotNull(message = "sceneId is required")
    private UUID sceneId;

    @NotNull(message = "deviceType is required")
    private DeviceType deviceType;

    private String deviceInfo;
}
