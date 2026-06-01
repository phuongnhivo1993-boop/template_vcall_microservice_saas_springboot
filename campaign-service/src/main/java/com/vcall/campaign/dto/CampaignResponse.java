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
public class CampaignResponse {

    private Long id;
    private String name;
    private String description;
    private String type;
    private String status;
    private String strategy;
    private LocalDateTime scheduleStart;
    private LocalDateTime scheduleEnd;
    private Long totalMembers;
    private Long completedMembers;
    private Double successRate;
    private LocalDateTime createdAt;
}
