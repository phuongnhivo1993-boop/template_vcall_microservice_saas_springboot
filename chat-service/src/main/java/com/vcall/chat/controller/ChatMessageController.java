package com.vcall.chat.controller;

import com.vcall.chat.dto.AttachmentResponse;
import com.vcall.chat.dto.ChatMessageRequest;
import com.vcall.chat.dto.ChatMessageResponse;
import com.vcall.chat.service.ChatMessageService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/chat/conversations/{id}/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable UUID id, @Valid @RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = chatMessageService.sendMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", response));
    }

    @PostMapping("/{messageId}/attachments")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable UUID id, @PathVariable UUID messageId, @RequestParam("file") MultipartFile file) {
        AttachmentResponse response = chatMessageService.addAttachment(id, messageId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attachment uploaded successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(@PathVariable UUID id, Pageable pageable) {
        Page<ChatMessageResponse> messages = chatMessageService.getMessages(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@PathVariable UUID id) {
        long count = chatMessageService.getUnreadCount(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PostMapping("/read")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        chatMessageService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }
}
