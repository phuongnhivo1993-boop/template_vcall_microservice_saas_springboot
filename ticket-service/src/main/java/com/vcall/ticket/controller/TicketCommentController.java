package com.vcall.ticket.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.ticket.dto.TicketCommentRequest;
import com.vcall.ticket.dto.TicketCommentResponse;
import com.vcall.ticket.entity.TicketComment.AuthorType;
import com.vcall.ticket.service.TicketCommentService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketCommentResponse>> addComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketCommentRequest request,
            @RequestParam(defaultValue = "SYSTEM") String authorType,
            @RequestParam(required = false) UUID authorId) {
        UUID resolvedAuthorId = authorId != null ? authorId : UUID.randomUUID();
        AuthorType type = AuthorType.valueOf(authorType.toUpperCase());
        TicketCommentResponse response = commentService.addComment(ticketId, request, resolvedAuthorId, type);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketCommentResponse>>> getComments(
            @PathVariable UUID ticketId, Pageable pageable) {
        Page<TicketCommentResponse> comments = commentService.getComments(ticketId, pageable);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/internal")
    public ResponseEntity<ApiResponse<Page<TicketCommentResponse>>> getInternalComments(
            @PathVariable UUID ticketId, Pageable pageable) {
        Page<TicketCommentResponse> comments = commentService.getInternalComments(ticketId, pageable);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
}
