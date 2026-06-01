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
public class ReportExecutionResponse {

    private Long id;
    private String reportName;
    private LocalDateTime executedAt;
    private String status;
    private String errorMessage;
    private Long executionTime;
}
