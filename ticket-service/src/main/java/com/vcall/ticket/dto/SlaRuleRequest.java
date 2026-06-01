package com.vcall.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlaRuleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String priority;

    private String category;

    private Integer firstResponseTime;

    private Integer resolutionTime;

    private Integer escalationLevel;

    private String escalationNotifyTo;

    private Boolean isActive = true;
}
