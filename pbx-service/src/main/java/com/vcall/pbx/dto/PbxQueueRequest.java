package com.vcall.pbx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PbxQueueRequest {

    @NotBlank(message = "Queue name is required")
    @Size(max = 255)
    private String name;

    private String description;

    @NotBlank(message = "Queue strategy is required")
    private String strategy;

    private Integer maxWaitTime;

    private Integer maxQueueSize;

    private String timeoutAction;

    @Size(max = 100)
    private String timeoutDestination;
}
