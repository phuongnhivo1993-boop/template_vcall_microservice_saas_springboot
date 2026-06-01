package com.vcall.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentPerformanceResponse {

    private UUID agentId;
    private String agentName;
    private String period;
    private Long totalCalls;
    private Long answeredCalls;
    private Long missedCalls;
    private Double avgTalkDuration;
    private Double occupancyRate;
}
