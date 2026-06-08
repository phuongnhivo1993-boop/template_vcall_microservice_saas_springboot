package com.vcall.scheduling.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.BulkStatusRequest;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.scheduling.dto.request.AppointmentRequest;
import com.vcall.scheduling.dto.response.AppointmentResponse;
import com.vcall.scheduling.entity.Appointment;
import com.vcall.scheduling.service.AppointmentService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "CRUD and search operations for appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
    @Operation(summary = "Create a new appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all appointments with pagination")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAllAppointments(Pageable pageable) {
        Page<AppointmentResponse> appointments = appointmentService.getAllAppointments(pageable);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(@PathVariable UUID id) {
        AppointmentResponse response = appointmentService.getAppointment(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
    @Operation(summary = "Update appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(@PathVariable UUID id,
                                                                               @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Delete appointment")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
    @Operation(summary = "Update appointment status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(@PathVariable UUID id,
                                                                          @RequestParam String status) {
        AppointmentResponse response = appointmentService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search appointments with filters")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> searchAppointments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID agentId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Specification<Appointment> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), Appointment.AppointmentStatus.valueOf(status.toUpperCase())));
        }
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agentId"), agentId));
        }
        if (customerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("customerId"), customerId));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("startTime"), startDate, endDate));
        } else if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), startDate));
        } else if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), endDate));
        }
        Page<AppointmentResponse> response = appointmentService.searchAppointments(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get appointments by customer ID")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByCustomer(@PathVariable UUID customerId) {
        List<AppointmentResponse> appointments = appointmentService.getByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @GetMapping("/agent/{agentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get appointments by agent ID")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByAgent(@PathVariable UUID agentId) {
        List<AppointmentResponse> appointments = appointmentService.getByAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get appointment statistics by status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        Map<String, Long> stats = appointmentService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Export appointments to CSV")
    public void exportCsv(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<Appointment> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), Appointment.AppointmentStatus.valueOf(status.toUpperCase())));
        }
        Page<AppointmentResponse> items = appointmentService.searchAppointments(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Title", "Customer ID", "Agent ID", "Start Time", "End Time", "Status", "Type", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "title", "customerId", "agentId", "startTime", "endTime", "status", "type", "createdAt"));
        CsvExportUtil.writeCsv(response, "appointments.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Export appointments to Excel")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String status,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<Appointment> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), Appointment.AppointmentStatus.valueOf(status.toUpperCase())));
        }
        Page<AppointmentResponse> items = appointmentService.searchAppointments(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Title", "Customer ID", "Agent ID", "Start Time", "End Time", "Status", "Type", "Created At");
        ExcelExportUtil.writeExcel(response, "appointments.xlsx", headers, items.getContent(),
                Arrays.asList("id", "title", "customerId", "agentId", "startTime", "endTime", "status", "type", "createdAt"));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Bulk delete appointments")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkDelete(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                appointmentService.deleteAppointment(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }

    @PostMapping("/bulk-status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Bulk status update for appointments")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkStatus(
            @RequestBody BulkStatusRequest request) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : request.getIds()) {
            try {
                appointmentService.updateStatus(id, request.getStatus());
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk status update completed", result));
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Import appointments from CSV")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<?>>> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {
        List<AppointmentRequest> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 6) {
                    AppointmentRequest request = new AppointmentRequest();
                    request.setTitle(fields[0].trim());
                    request.setDescription(fields.length > 1 ? fields[1].trim() : null);
                    request.setCustomerId(UUID.fromString(fields[2].trim()));
                    request.setAgentId(UUID.fromString(fields[3].trim()));
                    request.setStartTime(LocalDateTime.parse(fields[4].trim()));
                    request.setEndTime(LocalDateTime.parse(fields[5].trim()));
                    if (fields.length > 6) request.setType(fields[6].trim());
                    if (fields.length > 7) request.setLocation(fields[7].trim());
                    items.add(request);
                }
            }
        }
        BulkOperationUtil.BulkResult<?> result = BulkOperationUtil.bulkCreate(items, item ->
                appointmentService.createAppointment((AppointmentRequest) item));
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }
}
