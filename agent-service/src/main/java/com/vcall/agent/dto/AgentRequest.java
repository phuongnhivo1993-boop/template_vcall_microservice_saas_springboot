package com.vcall.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {

    private UUID userId;

    @NotBlank(message = "Agent code is required")
    @Size(max = 50)
    private String agentCode;

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    private String fullName;

    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String skill;

    private int maxConcurrentCalls = 5;

    private Set<Long> groupIds;
}
