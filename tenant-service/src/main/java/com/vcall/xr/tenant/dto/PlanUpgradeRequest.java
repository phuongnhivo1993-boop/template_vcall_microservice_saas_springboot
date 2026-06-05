package com.vcall.xr.tenant.dto;

import com.vcall.xr.tenant.domain.TenantPlan;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanUpgradeRequest {

    @NotNull(message = "Plan is required")
    private TenantPlan plan;

    private String billingCycle;
}
