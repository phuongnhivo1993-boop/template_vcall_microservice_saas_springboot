package com.vcall.scheduling.dto.response;

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
public class AppointmentResponse {

    private UUID id;
    private String title;
    private String description;
    private UUID customerId;
    private UUID agentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String type;
    private String location;
    private String meetingLink;
    private String notes;
    private Boolean reminderSent;
    private String recurrenceRule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
