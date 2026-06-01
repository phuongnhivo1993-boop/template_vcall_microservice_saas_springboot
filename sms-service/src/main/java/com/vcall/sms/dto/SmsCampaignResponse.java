package com.vcall.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsCampaignResponse {

    private Long id;
    private String name;
    private String description;
    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer totalRecipients;
    private Integer sentCount;
    private Integer failedCount;
    private LocalDateTime createdAt;
}
