package com.vcall.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentSessionRequest {

    @NotNull(message = "Agent ID is required")
    private UUID agentId;

    @NotBlank(message = "Session type is required")
    private String sessionType;
}
