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
public class AgentResponse {

    private UUID id;
    private UUID userId;
    private String agentCode;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private String skill;
    private int maxConcurrentCalls;
    private AgentSessionResponse currentSession;
    private LocalDateTime createdAt;
}
