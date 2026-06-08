package com.vcall.campaign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResultRequest {

    private Long memberId;

    private UUID agentId;

    private UUID callId;

    @NotBlank
    private String resultType;

    private Integer duration;

    private String notes;

    private String disposition;
}
