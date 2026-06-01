package com.vcall.billing.dto;

import com.vcall.billing.entity.UsageRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecordResponse {

    private Long id;
    private UsageRecord.UsageType usageType;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalCost;
    private LocalDateTime recordedAt;
    private UsageRecord.UsageSource source;
}
