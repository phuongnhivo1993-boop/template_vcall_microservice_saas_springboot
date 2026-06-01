package com.vcall.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentGroupRequest {

    @NotBlank(message = "Group name is required")
    private String name;

    private String description;
}
