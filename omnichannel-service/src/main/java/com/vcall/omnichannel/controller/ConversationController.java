package com.vcall.omnichannel.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.omnichannel.dto.request.ConversationAssignRequest;
import com.vcall.omnichannel.dto.request.ConversationRequest;
import com.vcall.omnichannel.dto.request.ConversationStatusRequest;
import com.vcall.omnichannel.dto.request.ConversationUpdateRequest;
import com.vcall.omnichannel.dto.response.ConversationResponse;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.Conversation.ConversationStatus;
import com.vcall.omnichannel.service.ConversationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/omnichannel/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> create(@Valid @RequestBody ConversationRequest request) {
        ConversationResponse response = conversationService.createConversation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Conversation created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getById(@PathVariable UUID id) {
        ConversationResponse response = conversationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody ConversationUpdateRequest request) {
        ConversationResponse response = conversationService.updateConversation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Conversation updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", null));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ConversationResponse>> assign(
            @PathVariable UUID id,
            @Valid @RequestBody ConversationAssignRequest request) {
        ConversationResponse response = conversationService.assignAgent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Conversation assigned", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ConversationResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ConversationStatusRequest request) {
        ConversationResponse response = conversationService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated", response));
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> getByChannel(@PathVariable Channel channel, Pageable pageable) {
        Page<ConversationResponse> responses = conversationService.getByChannel(channel, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> search(
            @RequestParam(required = false) Channel channel,
            @RequestParam(required = false) ConversationStatus status,
            @RequestParam(required = false) UUID agentId,
            Pageable pageable) {
        Page<ConversationResponse> responses = conversationService.search(channel, status, agentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
