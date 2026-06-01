package com.vcall.sipservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipDeviceRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String deviceType;

    private String userAgent;

    private String ipAddress;

    private String macAddress;

    private String firmwareVersion;
}
