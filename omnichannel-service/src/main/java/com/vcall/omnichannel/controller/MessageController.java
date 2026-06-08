package com.vcall.omnichannel.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.omnichannel.dto.request.MessageRequest;
import com.vcall.omnichannel.dto.response.MessageResponse;
import com.vcall.omnichannel.service.MessageService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/omnichannel/conversations/{id}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<MessageResponse>> send(
            @PathVariable UUID id,
            @Valid @RequestBody MessageRequest request) {
        MessageResponse response = messageService.sendMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Message sent", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(@PathVariable UUID id, Pageable pageable) {
        Page<MessageResponse> responses = messageService.getMessages(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
