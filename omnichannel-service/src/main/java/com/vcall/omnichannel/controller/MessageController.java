package com.vcall.omnichannel.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.omnichannel.dto.request.MessageRequest;
import com.vcall.omnichannel.dto.response.MessageResponse;
import com.vcall.omnichannel.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/omnichannel/conversations/{id}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> send(
            @PathVariable UUID id,
            @Valid @RequestBody MessageRequest request) {
        MessageResponse response = messageService.sendMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Message sent", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(@PathVariable UUID id) {
        List<MessageResponse> responses = messageService.getMessages(id);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
