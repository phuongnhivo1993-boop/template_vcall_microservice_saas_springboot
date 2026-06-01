package com.vcall.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentGroupResponse {

    private Long id;
    private String name;
    private String description;
    private long memberCount;
}
