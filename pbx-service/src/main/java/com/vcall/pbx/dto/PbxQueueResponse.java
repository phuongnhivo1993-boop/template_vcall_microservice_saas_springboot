package com.vcall.pbx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PbxQueueResponse {

    private Long id;
    private String name;
    private String description;
    private String strategy;
    private Integer maxWaitTime;
    private Integer maxQueueSize;
    private String timeoutAction;
    private String timeoutDestination;
    private int memberCount;
}
