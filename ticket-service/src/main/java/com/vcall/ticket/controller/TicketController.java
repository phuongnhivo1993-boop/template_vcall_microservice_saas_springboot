package com.vcall.ticket.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.ticket.dto.TicketAssignRequest;
import com.vcall.ticket.dto.TicketRequest;
import com.vcall.ticket.dto.TicketResponse;
import com.vcall.ticket.dto.TicketStatusRequest;
import com.vcall.ticket.entity.Ticket.TicketStatus;
import com.vcall.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(@Valid @RequestBody TicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getAllTickets(Pageable pageable) {
        Page<TicketResponse> tickets = ticketService.getAllTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(@PathVariable UUID id) {
        TicketResponse response = ticketService.getTicket(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketResponse>> getByTicketNumber(@PathVariable String ticketNumber) {
        TicketResponse response = ticketService.getByTicketNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(@PathVariable UUID id,
                                                                     @Valid @RequestBody TicketRequest request) {
        TicketResponse response = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> updateStatus(@PathVariable UUID id,
                                                                     @Valid @RequestBody TicketStatusRequest request) {
        TicketResponse response = ticketService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(@PathVariable UUID id,
                                                                     @Valid @RequestBody TicketAssignRequest request) {
        TicketResponse response = ticketService.assignTicket(id, request);
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully", response));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<ApiResponse<TicketResponse>> escalateTicket(@PathVariable UUID id,
                                                                       @RequestParam(defaultValue = "Escalated") String reason) {
        TicketResponse response = ticketService.escalateTicket(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Ticket escalated successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TicketResponse> results = ticketService.search(q, status, priority, assignedTo, startDate, endDate,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByStatus() {
        Map<String, Long> stats = ticketService.getStatsByStatus();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
