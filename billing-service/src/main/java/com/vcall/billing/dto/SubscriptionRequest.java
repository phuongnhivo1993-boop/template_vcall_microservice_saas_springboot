package com.vcall.billing.dto;

import jakarta.validation.constraints.NotNull;
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
public class SubscriptionRequest {

    @NotNull
    private Long planId;

    @NotNull
    private UUID subscriberId;

    @NotNull
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean autoRenew;

    private LocalDateTime trialEndDate;
}
