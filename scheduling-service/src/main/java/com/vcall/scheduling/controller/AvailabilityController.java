package com.vcall.scheduling.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.scheduling.dto.request.AvailabilityCheckRequest;
import com.vcall.scheduling.dto.response.AvailabilityResponse;
import com.vcall.scheduling.entity.AgentAvailability;
import com.vcall.scheduling.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Tag(name = "Agent Availability", description = "Agent availability and working hours management")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
    @Operation(summary = "Create availability slot")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> createAvailability(@Valid @RequestBody AgentAvailability availability) {
        AvailabilityResponse response = availabilityService.createAvailability(availability);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all availability slots")
    public ResponseEntity<ApiResponse<Page<AvailabilityResponse>>> getAllAvailabilities(Pageable pageable) {
        Page<AvailabilityResponse> availabilities = availabilityService.getAllAvailabilities(pageable);
        return ResponseEntity.ok(ApiResponse.success(availabilities));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get availability by ID")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(@PathVariable UUID id) {
        AvailabilityResponse response = availabilityService.getAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
    @Operation(summary = "Delete availability slot")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(@PathVariable UUID id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.ok(ApiResponse.success("Availability deleted successfully", null));
    }

    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if an agent is available at a given time")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAvailability(@Valid @RequestBody AvailabilityCheckRequest request) {
        boolean available = availabilityService.checkAvailability(
                request.getAgentId(), request.getDate(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok(ApiResponse.success(Map.of("available", available)));
    }

    @GetMapping("/agent/{agentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get availability by agent ID")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getByAgent(@PathVariable UUID agentId) {
        List<AvailabilityResponse> availabilities = availabilityService.getByAgentAndDate(agentId, LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success(availabilities));
    }

    @GetMapping("/agent/{agentId}/date/{date}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get availability by agent and date")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getByAgentAndDate(
            @PathVariable UUID agentId, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<AvailabilityResponse> availabilities = availabilityService.getByAgentAndDate(agentId, localDate);
        return ResponseEntity.ok(ApiResponse.success(availabilities));
    }

    @GetMapping("/agent/{agentId}/range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get availability by agent and date range")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getByAgentAndDateRange(
            @PathVariable UUID agentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<AvailabilityResponse> availabilities = availabilityService.getByAgentAndDateRange(
                agentId, LocalDate.parse(startDate), LocalDate.parse(endDate));
        return ResponseEntity.ok(ApiResponse.success(availabilities));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
    @Operation(summary = "Update availability status")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> updateStatus(@PathVariable UUID id,
                                                                           @RequestParam String status) {
        AvailabilityResponse response = availabilityService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Availability status updated successfully", response));
    }

    @PatchMapping("/{id}/toggle-booked")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
    @Operation(summary = "Toggle booked status")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> toggleBooked(@PathVariable UUID id) {
        AvailabilityResponse response = availabilityService.toggleBooked(id);
        return ResponseEntity.ok(ApiResponse.success("Booked status toggled successfully", response));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search availability slots")
    public ResponseEntity<ApiResponse<Page<AvailabilityResponse>>> search(
            @RequestParam(required = false) UUID agentId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Specification<AgentAvailability> spec = Specification.where(null);
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agentId"), agentId));
        }
        if (date != null && !date.isEmpty()) {
            LocalDate localDate = LocalDate.parse(date);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("date"), localDate));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), AgentAvailability.AvailabilityStatus.valueOf(status.toUpperCase())));
        }
        Page<AvailabilityResponse> response = availabilityService.searchAvailabilities(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export availability to CSV")
    public void exportCsv(@RequestParam(required = false) UUID agentId,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("date").ascending());
        Specification<AgentAvailability> spec = Specification.where(null);
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agentId"), agentId));
        }
        Page<AvailabilityResponse> items = availabilityService.searchAvailabilities(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Agent ID", "Date", "Start Time", "End Time", "Booked", "Status");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "agentId", "date", "startTime", "endTime", "isBooked", "status"));
        CsvExportUtil.writeCsv(response, "availability.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export availability to Excel")
    public void exportExcel(@RequestParam(required = false) UUID agentId,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("date").ascending());
        Specification<AgentAvailability> spec = Specification.where(null);
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agentId"), agentId));
        }
        Page<AvailabilityResponse> items = availabilityService.searchAvailabilities(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Agent ID", "Date", "Start Time", "End Time", "Booked", "Status");
        ExcelExportUtil.writeExcel(response, "availability.xlsx", headers, items.getContent(),
                Arrays.asList("id", "agentId", "date", "startTime", "endTime", "isBooked", "status"));
    }
}
