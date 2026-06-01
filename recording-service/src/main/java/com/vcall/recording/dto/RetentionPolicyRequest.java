package com.vcall.recording.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetentionPolicyRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Retention days is required")
    @Positive(message = "Retention days must be positive")
    private Integer retentionDays;

    @NotBlank(message = "Action is required")
    private String action;

    private boolean isActive = true;
}
