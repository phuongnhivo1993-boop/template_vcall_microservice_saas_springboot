package com.vcall.call.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleRequest {

    @NotBlank
    private String name;

    @NotNull
    private Integer priority;

    private String condition;

    @NotBlank
    private String destination;

    private Long destinationId;

    private String timeProfile;

    private boolean isActive;
}
