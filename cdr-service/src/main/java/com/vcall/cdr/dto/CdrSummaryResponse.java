package com.vcall.cdr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdrSummaryResponse {

    private LocalDate date;
    private Long totalCalls;
    private Long answeredCalls;
    private Long missedCalls;
    private Long totalDuration;
    private Double avgDuration;
    private BigDecimal totalCost;
    private Integer maxConcurrentCalls;
}
