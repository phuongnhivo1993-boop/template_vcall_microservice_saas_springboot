package com.vcall.chat.controller;

import com.vcall.chat.dto.ChatAssignRequest;
import com.vcall.chat.dto.ChatConversationRequest;
import com.vcall.chat.dto.ChatConversationResponse;
import com.vcall.chat.dto.ChatConversationUpdateRequest;
import com.vcall.chat.entity.ChatConversation;
import com.vcall.chat.entity.ChatConversation.Status;
import com.vcall.chat.service.ChatMessageService;
import com.vcall.chat.service.ChatService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/chat/conversations")
@RequiredArgsConstructor
public class ChatConversationController {

    private final ChatService chatService;
    private final ChatMessageService chatMessageService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> createConversation(
            @Valid @RequestBody ChatConversationRequest request) {
        ChatConversationResponse response = chatService.createConversation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> getConversation(@PathVariable UUID id) {
        ChatConversationResponse response = chatService.getConversationHistory(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> updateConversation(
            @PathVariable UUID id, @Valid @RequestBody ChatConversationUpdateRequest request) {
        ChatConversationResponse response = chatService.updateConversation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Conversation updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable UUID id) {
        chatService.deleteConversation(id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully", null));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> assignAgent(
            @PathVariable UUID id, @Valid @RequestBody ChatAssignRequest request) {
        ChatConversationResponse response = chatService.assignAgent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Agent assigned successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> updateStatus(@PathVariable UUID id) {
        ChatConversationResponse response = chatService.closeConversation(id);
        return ResponseEntity.ok(ApiResponse.success("Conversation closed successfully", response));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ChatConversationResponse>>> getActiveConversations(Pageable pageable) {
        Page<ChatConversationResponse> conversations = chatService.getActiveConversations(pageable);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ChatConversationResponse>>> getAllConversations(Pageable pageable) {
        Page<ChatConversationResponse> conversations = chatService.getByStatus(Status.ACTIVE, pageable);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ChatConversationResponse>>> getConversationsByAgent(
            @PathVariable UUID agentId, Pageable pageable) {
        Page<ChatConversationResponse> conversations = chatService.getByAgentId(agentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        chatMessageService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ChatConversationResponse>>> searchConversations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) UUID agentId,
            Pageable pageable) {
        Page<ChatConversationResponse> result;
        if (agentId != null) {
            result = chatService.getByAgentId(agentId, pageable);
        } else if (status != null) {
            result = chatService.getByStatus(ChatConversation.Status.valueOf(status.toUpperCase()), pageable);
        } else {
            result = chatService.getByStatus(ChatConversation.Status.ACTIVE, pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> exportCsv() {
        List<ChatConversationResponse> all = chatService.getAllConversations();
        return ResponseEntity.ok(ApiResponse.success(all));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> exportExcel() {
        List<ChatConversationResponse> all = chatService.getAllConversations();
        return ResponseEntity.ok(ApiResponse.success(all));
    }
}
