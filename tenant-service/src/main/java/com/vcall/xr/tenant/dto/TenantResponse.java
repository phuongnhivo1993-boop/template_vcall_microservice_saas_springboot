package com.vcall.xr.tenant.dto;

import com.vcall.xr.tenant.domain.TenantPlan;
import com.vcall.xr.tenant.domain.TenantStatus;
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
public class TenantResponse {

    private UUID id;
    private String name;
    private String slug;
    private TenantPlan plan;
    private TenantStatus status;
    private Integer maxScenes;
    private Long maxStorageGb;
    private Long maxBandwidthGb;
    private String features;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
