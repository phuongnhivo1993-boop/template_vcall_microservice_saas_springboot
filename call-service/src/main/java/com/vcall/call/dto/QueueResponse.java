package com.vcall.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueResponse {

    private Long id;
    private String name;
    private String strategy;
    private Integer maxWaitTime;
    private Integer maxQueueSize;
    private long memberCount;
}
