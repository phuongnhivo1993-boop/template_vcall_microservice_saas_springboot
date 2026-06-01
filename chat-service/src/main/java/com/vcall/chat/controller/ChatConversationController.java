package com.vcall.chat.controller;

import com.vcall.chat.dto.ChatAssignRequest;
import com.vcall.chat.dto.ChatConversationRequest;
import com.vcall.chat.dto.ChatConversationResponse;
import com.vcall.chat.entity.ChatConversation.Status;
import com.vcall.chat.service.ChatService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat/conversations")
@RequiredArgsConstructor
public class ChatConversationController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatConversationResponse>> createConversation(
            @Valid @RequestBody ChatConversationRequest request) {
        ChatConversationResponse response = chatService.createConversation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> getConversation(@PathVariable UUID id) {
        ChatConversationResponse response = chatService.getConversationHistory(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> assignAgent(
            @PathVariable UUID id, @Valid @RequestBody ChatAssignRequest request) {
        ChatConversationResponse response = chatService.assignAgent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Agent assigned successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> updateStatus(@PathVariable UUID id) {
        ChatConversationResponse response = chatService.closeConversation(id);
        return ResponseEntity.ok(ApiResponse.success("Conversation closed successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> getActiveConversations() {
        List<ChatConversationResponse> conversations = chatService.getActiveConversations();
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> getAllConversations() {
        List<ChatConversationResponse> conversations = chatService.getByStatus(Status.ACTIVE);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> getConversationsByAgent(
            @PathVariable UUID agentId) {
        List<ChatConversationResponse> conversations = chatService.getByAgentId(agentId);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }
}
