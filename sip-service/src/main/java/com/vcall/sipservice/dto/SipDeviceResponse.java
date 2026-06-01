package com.vcall.sipservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipDeviceResponse {
    private Long id;
    private String name;
    private String deviceType;
    private String userAgent;
    private String ipAddress;
    private String macAddress;
    private String firmwareVersion;
}
