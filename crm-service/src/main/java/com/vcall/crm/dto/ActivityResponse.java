package com.vcall.crm.dto;

import com.vcall.crm.entity.ActivityType;
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
public class ActivityResponse {

    private Long id;
    private UUID customerId;
    private UUID leadId;
    private ActivityType type;
    private String subject;
    private String description;
    private LocalDateTime activityDate;
    private Integer duration;
    private UUID assignedTo;
    private String result;
    private LocalDateTime createdAt;
}
