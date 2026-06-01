package com.vcall.crm.dto;

import com.vcall.crm.entity.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ActivityRequest {

    @NotNull
    private UUID customerId;

    private UUID leadId;

    @NotNull
    private ActivityType type;

    @NotBlank
    private String subject;

    private String description;

    private LocalDateTime activityDate;

    private Integer duration;

    private UUID assignedTo;

    private String result;
}
