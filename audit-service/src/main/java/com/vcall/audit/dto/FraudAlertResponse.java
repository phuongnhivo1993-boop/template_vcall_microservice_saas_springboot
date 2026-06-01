package com.vcall.audit.dto;

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
public class FraudAlertResponse {
    private UUID id;
    private LocalDateTime detectedAt;
    private String alertType;
    private String severity;
    private UUID actorId;
    private String description;
    private String status;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
}
