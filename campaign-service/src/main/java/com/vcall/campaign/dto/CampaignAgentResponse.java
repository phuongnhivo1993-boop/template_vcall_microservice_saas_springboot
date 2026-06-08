package com.vcall.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignAgentResponse {

    private Long id;
    private Long campaignId;
    private UUID agentId;
    private Integer maxConcurrentCalls;
    private Boolean isActive;
}
