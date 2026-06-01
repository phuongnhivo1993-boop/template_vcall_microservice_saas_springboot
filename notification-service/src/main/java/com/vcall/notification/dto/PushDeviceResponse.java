package com.vcall.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushDeviceResponse {

    private Long id;
    private UUID userId;
    private String deviceToken;
    private String platform;
    private Boolean isActive;
}
