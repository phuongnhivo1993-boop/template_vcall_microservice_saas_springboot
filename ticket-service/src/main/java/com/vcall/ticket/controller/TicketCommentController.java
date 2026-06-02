package com.vcall.ticket.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.ticket.dto.TicketCommentRequest;
import com.vcall.ticket.dto.TicketCommentResponse;
import com.vcall.ticket.entity.TicketComment.AuthorType;
import com.vcall.ticket.service.TicketCommentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TicketCommentResponse>>> search(
            @PathVariable UUID ticketId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String authorType,
            Pageable pageable) {
        Page<TicketCommentResponse> result = commentService.search(ticketId, keyword, authorType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@PathVariable UUID ticketId,
                          @RequestParam(required = false) String keyword,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<TicketCommentResponse> items = commentService.search(ticketId, keyword, null, pageable);
        List<String> headers = Arrays.asList("ID", "Ticket ID", "Content", "Author ID", "Author Type", "Internal", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "ticketId", "content", "authorId", "authorType", "isInternal", "createdAt"));
        CsvExportUtil.writeCsv(response, "ticket-comments.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@PathVariable UUID ticketId,
                            @RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<TicketCommentResponse> items = commentService.search(ticketId, keyword, null, pageable);
        List<String> headers = Arrays.asList("ID", "Ticket ID", "Content", "Author ID", "Author Type", "Internal", "Created At");
        ExcelExportUtil.writeExcel(response, "ticket-comments.xlsx", headers, items.getContent(),
                Arrays.asList("id", "ticketId", "content", "authorId", "authorType", "isInternal", "createdAt"));
    }
}
