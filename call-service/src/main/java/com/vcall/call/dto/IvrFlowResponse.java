package com.vcall.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IvrFlowResponse {

    private Long id;
    private String name;
    private String description;
    private String greetingMessage;
    private String fallbackDestination;
    private Integer timeout;
    private List<IvrStepRequest> steps;
}
