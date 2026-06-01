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
public class IvrFlowRequest {

    @NotBlank
    private String name;

    private String description;

    private String greetingMessage;

    private String fallbackDestination;

    private Integer timeout;
}
