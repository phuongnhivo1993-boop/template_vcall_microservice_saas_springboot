package com.vcall.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignMemberResponse {

    private Long id;
    private Long campaignId;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Integer priority;
    private String status;
    private Integer attempts;
    private LocalDateTime lastDialedAt;
    private String result;
}
