package com.vcall.pbx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RingGroupRequest {

    @NotBlank(message = "Ring group name is required")
    @Size(max = 255)
    private String name;

    private String description;

    @NotBlank(message = "Ring strategy is required")
    private String strategy;

    private Integer ringTimeout;

    @Size(max = 100)
    private String ringbackTone;
}
