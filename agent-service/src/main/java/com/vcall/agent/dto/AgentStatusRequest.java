package com.vcall.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String reason;
}
