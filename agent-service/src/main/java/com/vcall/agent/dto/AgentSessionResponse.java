package com.vcall.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSessionResponse {

    private Long id;
    private UUID agentId;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private Long duration;
    private String sessionType;
}
