package com.vcall.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinitionResponse {

    private Long id;
    private String name;
    private String description;
    private String reportType;
    private String schedule;
    private boolean isActive;
    private LocalDateTime lastExecutedAt;
    private LocalDateTime createdAt;
}
