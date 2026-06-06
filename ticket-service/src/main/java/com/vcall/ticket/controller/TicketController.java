package com.vcall.ticket.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.BulkStatusRequest;
import com.vcall.common.dto.PagedResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.ticket.dto.TicketAssignRequest;
import com.vcall.ticket.dto.TicketRequest;
import com.vcall.ticket.dto.TicketResponse;
import com.vcall.ticket.dto.TicketStatusRequest;
import com.vcall.ticket.entity.Ticket.TicketStatus;
import com.vcall.ticket.service.TicketService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> getAllTickets(Pageable pageable) {
        Page<TicketResponse> tickets = ticketService.getAllTickets(pageable);
        PagedResponse<TicketResponse> paged = PagedResponse.<TicketResponse>builder()
                .content(tickets.getContent())
                .page(tickets.getNumber())
                .size(tickets.getSize())
                .totalElements(tickets.getTotalElements())
                .totalPages(tickets.getTotalPages())
                .last(tickets.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(paged));
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
    public ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> search(
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
        PagedResponse<TicketResponse> paged = PagedResponse.<TicketResponse>builder()
                .content(results.getContent())
                .page(results.getNumber())
                .size(results.getSize())
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .last(results.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByStatus() {
        Map<String, Long> stats = ticketService.getStatsByStatus();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam(required = false) String q,
                          @RequestParam(required = false) String status,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TicketResponse> items = ticketService.search(q, status, null, null, null, null, pageable);
        List<String> headers = Arrays.asList("ID", "Ticket Number", "Title", "Customer ID", "Source", "Category", "Priority", "Status", "Assigned To", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "ticketNumber", "title", "customerId", "source", "category", "priority", "status", "assignedTo", "createdAt"));
        CsvExportUtil.writeCsv(response, "tickets.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) String q,
                            @RequestParam(required = false) String status,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TicketResponse> items = ticketService.search(q, status, null, null, null, null, pageable);
        List<String> headers = Arrays.asList("ID", "Ticket Number", "Title", "Customer ID", "Source", "Category", "Priority", "Status", "Assigned To", "Created At");
        ExcelExportUtil.writeExcel(response, "tickets.xlsx", headers, items.getContent(),
                Arrays.asList("id", "ticketNumber", "title", "customerId", "source", "category", "priority", "status", "assignedTo", "createdAt"));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkDelete(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                ticketService.deleteTicket(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkStatus(
            @RequestBody BulkStatusRequest request) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        TicketStatusRequest statusRequest = new TicketStatusRequest(request.getStatus(), request.getReason());
        for (UUID id : request.getIds()) {
            try {
                ticketService.updateStatus(id, statusRequest);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk status update completed", result));
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<?>>> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {
        List<TicketRequest> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 4) {
                    TicketRequest request = new TicketRequest();
                    request.setTitle(fields[0].trim());
                    request.setDescription(fields.length > 1 ? fields[1].trim() : null);
                    request.setCustomerId(UUID.fromString(fields[2].trim()));
                    request.setSource(fields[3].trim());
                    if (fields.length > 4) request.setCategory(fields[4].trim());
                    if (fields.length > 5) request.setPriority(fields[5].trim());
                    if (fields.length > 6 && !fields[6].trim().isEmpty())
                        request.setAssignedTo(UUID.fromString(fields[6].trim()));
                    items.add(request);
                }
            }
        }
        BulkOperationUtil.BulkResult<?> result = BulkOperationUtil.bulkCreate(items, item ->
                ticketService.createTicket((TicketRequest) item));
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }
}
