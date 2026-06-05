package com.vcall.xr.tenant.dto;

import com.vcall.xr.tenant.domain.TenantPlan;
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
public class TenantRequest {

    @NotBlank(message = "Tenant name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotNull(message = "Plan is required")
    private TenantPlan plan;

    private Integer maxScenes;

    private Long maxStorageGb;

    private Long maxBandwidthGb;

    private String features;
}
