package com.vcall.campaign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRequest {

    @NotBlank(message = "Campaign name is required")
    @Size(max = 255)
    private String name;

    private String description;

    @NotBlank(message = "Campaign type is required")
    private String type;

    private String strategy;

    private LocalDateTime scheduleStart;

    private LocalDateTime scheduleEnd;

    @Size(max = 50)
    private String timezone;

    @Size(max = 50)
    private String callerId;

    private LocalTime dailyStart;

    private LocalTime dailyEnd;

    private Integer maxAttempts = 3;

    private Integer retryInterval;

    private Integer agentIdleThreshold;
}
