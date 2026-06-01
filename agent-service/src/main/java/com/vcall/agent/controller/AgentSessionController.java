package com.vcall.agent.controller;

import com.vcall.agent.dto.AgentSessionRequest;
import com.vcall.agent.dto.AgentSessionResponse;
import com.vcall.agent.service.AgentSessionService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent-sessions")
@RequiredArgsConstructor
public class AgentSessionController {

    private final AgentSessionService agentSessionService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<AgentSessionResponse>> startSession(@Valid @RequestBody AgentSessionRequest request) {
        AgentSessionResponse response = agentSessionService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session started successfully", response));
    }

    @PutMapping("/{id}/end")
    public ResponseEntity<ApiResponse<AgentSessionResponse>> endSession(@PathVariable Long id) {
        AgentSessionResponse response = agentSessionService.endSession(id);
        return ResponseEntity.ok(ApiResponse.success("Session ended successfully", response));
    }

    @GetMapping("/active/{agentId}")
    public ResponseEntity<ApiResponse<AgentSessionResponse>> getActiveSession(@PathVariable UUID agentId) {
        AgentSessionResponse response = agentSessionService.getActiveSession(agentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
