package com.vcall.cdr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdrAnalyticsResponse {

    private Map<String, Object> callVolume;
    private Map<String, Object> agentPerformance;
    private Map<String, Object> costAnalysis;
    private Map<String, Object> concurrentCalls;
    private BigDecimal totalCost;
    private Long totalCalls;
    private Long answeredCalls;
    private Long missedCalls;
    private Double avgDuration;
}
