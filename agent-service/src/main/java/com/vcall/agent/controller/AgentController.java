package com.vcall.agent.controller;

import com.vcall.agent.dto.AgentRequest;
import com.vcall.agent.dto.AgentResponse;
import com.vcall.agent.dto.AgentStatusRequest;
import com.vcall.agent.dto.AgentStatusResponse;
import com.vcall.agent.entity.Agent.AgentStatusEnum;
import com.vcall.agent.service.AgentService;
import com.vcall.agent.service.AgentStatusService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final AgentStatusService agentStatusService;

    @PostMapping
    public ResponseEntity<ApiResponse<AgentResponse>> createAgent(@Valid @RequestBody AgentRequest request) {
        AgentResponse response = agentService.createAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agent created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AgentResponse>>> getAllAgents() {
        List<AgentResponse> agents = agentService.getAllAgents();
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentResponse>> getAgent(@PathVariable UUID id) {
        AgentResponse response = agentService.getAgent(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentResponse>> updateAgent(@PathVariable UUID id,
                                                                   @Valid @RequestBody AgentRequest request) {
        AgentResponse response = agentService.updateAgent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Agent updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAgent(@PathVariable UUID id) {
        agentService.deleteAgent(id);
        return ResponseEntity.ok(ApiResponse.success("Agent deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AgentResponse>> updateStatus(@PathVariable UUID id,
                                                                    @Valid @RequestBody AgentStatusRequest request) {
        AgentResponse response = agentService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AgentResponse>>> getAgentsByStatus(@PathVariable String status) {
        AgentStatusEnum statusEnum = AgentStatusEnum.valueOf(status.toUpperCase());
        List<AgentResponse> agents = agentService.getByStatus(statusEnum);
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<AgentStatusResponse>>> getStatusHistory(@PathVariable UUID id) {
        List<AgentStatusResponse> history = agentStatusService.getStatusHistory(id, null, null);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AgentResponse>> getProfile(@RequestParam UUID userId) {
        AgentResponse response = agentService.getAgentByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = agentService.getAgentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
