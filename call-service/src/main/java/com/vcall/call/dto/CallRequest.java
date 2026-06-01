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
public class CallRequest {

    @NotBlank
    private String callerNumber;

    private String calleeNumber;

    private String callerName;

    @NotBlank
    private String direction;

    private Long queueId;

    private Long ivrFlowId;
}
