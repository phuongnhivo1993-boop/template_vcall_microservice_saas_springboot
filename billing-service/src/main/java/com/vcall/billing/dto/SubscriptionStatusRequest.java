package com.vcall.billing.dto;

import com.vcall.billing.entity.Subscription.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusRequest {

    @NotNull(message = "Status is required")
    private SubscriptionStatus status;
}
