package com.vcall.billing.dto;

import com.vcall.billing.entity.PricingPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPlanRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private PricingPlan.PlanType planType;

    @NotNull
    private BigDecimal price;

    private String currency;

    @NotNull
    private PricingPlan.BillingCycle billingCycle;

    private String features;

    private Boolean isActive;
}
