package com.vcall.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaRuleResponse {

    private Long id;
    private String name;
    private String priority;
    private String category;
    private Integer firstResponseTime;
    private Integer resolutionTime;
    private Integer escalationLevel;
    private Boolean isActive;
}
