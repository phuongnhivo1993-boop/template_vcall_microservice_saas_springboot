package com.vcall.agent.controller;

import com.vcall.agent.dto.AgentGroupRequest;
import com.vcall.agent.dto.AgentGroupResponse;
import com.vcall.agent.service.AgentGroupService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent-groups")
@RequiredArgsConstructor
public class AgentGroupController {

    private final AgentGroupService agentGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<AgentGroupResponse>> createGroup(@Valid @RequestBody AgentGroupRequest request) {
        AgentGroupResponse response = agentGroupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AgentGroupResponse>>> getAllGroups(Pageable pageable) {
        Page<AgentGroupResponse> groups = agentGroupService.getAllGroups(pageable);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentGroupResponse>> getGroup(@PathVariable Long id) {
        AgentGroupResponse response = agentGroupService.getGroup(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentGroupResponse>> updateGroup(@PathVariable Long id,
                                                                        @Valid @RequestBody AgentGroupRequest request) {
        AgentGroupResponse response = agentGroupService.updateGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        agentGroupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully", null));
    }

    @PostMapping("/{groupId}/agents/{agentId}")
    public ResponseEntity<ApiResponse<Void>> addMember(@PathVariable Long groupId, @PathVariable UUID agentId) {
        agentGroupService.addMember(groupId, agentId);
        return ResponseEntity.ok(ApiResponse.success("Agent added to group successfully", null));
    }

    @DeleteMapping("/{groupId}/agents/{agentId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long groupId, @PathVariable UUID agentId) {
        agentGroupService.removeMember(groupId, agentId);
        return ResponseEntity.ok(ApiResponse.success("Agent removed from group successfully", null));
    }
}
