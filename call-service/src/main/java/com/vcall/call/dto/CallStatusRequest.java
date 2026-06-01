package com.vcall.call.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallStatusRequest {

    @NotBlank
    private String status;

    private UUID agentId;

    private String hangupCause;
}
