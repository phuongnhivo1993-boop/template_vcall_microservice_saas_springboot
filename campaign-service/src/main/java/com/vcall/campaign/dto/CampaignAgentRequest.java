package com.vcall.campaign.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignAgentRequest {

    @NotNull
    private UUID agentId;

    private Integer maxConcurrentCalls = 5;
}
