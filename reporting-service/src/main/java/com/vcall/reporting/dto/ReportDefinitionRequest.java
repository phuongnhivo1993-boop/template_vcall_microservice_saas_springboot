package com.vcall.reporting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinitionRequest {

    @NotBlank(message = "Report name is required")
    private String name;

    private String description;

    @NotBlank(message = "Report type is required")
    private String reportType;

    private String parameters;

    private String schedule;

    private String recipients;

    private boolean isActive = true;
}
