package com.vcall.call.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueRequest {

    @NotBlank
    private String name;

    private String strategy;

    private Integer maxWaitTime;

    private Integer maxQueueSize;
}
