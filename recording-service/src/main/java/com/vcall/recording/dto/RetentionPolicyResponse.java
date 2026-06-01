package com.vcall.recording.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetentionPolicyResponse {

    private Long id;
    private String name;
    private String description;
    private Integer retentionDays;
    private String action;
    private boolean isActive;
}
