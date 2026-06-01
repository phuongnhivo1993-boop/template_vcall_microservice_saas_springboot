package com.vcall.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleResponse {

    private Long id;
    private String name;
    private Integer priority;
    private String condition;
    private String destination;
    private Long destinationId;
    private String timeProfile;
    private boolean isActive;
}
