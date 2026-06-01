package com.vcall.billing.dto;

import com.vcall.billing.entity.PricingPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPlanResponse {

    private Long id;
    private String name;
    private String description;
    private PricingPlan.PlanType planType;
    private BigDecimal price;
    private String currency;
    private PricingPlan.BillingCycle billingCycle;
    private String features;
    private Boolean isActive;
}
