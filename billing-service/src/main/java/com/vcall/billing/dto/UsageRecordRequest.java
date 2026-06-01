package com.vcall.billing.dto;

import com.vcall.billing.entity.UsageRecord;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecordRequest {

    @NotNull
    private UUID subscriberId;

    @NotNull
    private UsageRecord.UsageType usageType;

    @NotNull
    private BigDecimal quantity;

    private BigDecimal unitPrice;

    @NotNull
    private UsageRecord.UsageSource source;

    private String sourceId;
}
