package com.vcall.xr.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagResponse {

    private UUID id;
    private UUID tenantId;
    private String featureKey;
    private boolean enabled;
    private String config;
    private LocalDateTime createdAt;
}
