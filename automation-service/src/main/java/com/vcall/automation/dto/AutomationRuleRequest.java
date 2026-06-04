package com.vcall.automation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AutomationRuleRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private String trigger;

    private String action;

    private Boolean isActive;
}
