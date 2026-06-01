package com.vcall.campaign.dto;

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
public class CampaignResultResponse {

    private Long id;
    private Long campaignId;
    private UUID agentId;
    private UUID callId;
    private String resultType;
    private Integer duration;
    private String notes;
    private String disposition;
    private LocalDateTime createdAt;
}
