package com.vcall.scheduling.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.scheduling.dto.request.ScheduleTemplateRequest;
import com.vcall.scheduling.dto.response.ScheduleTemplateResponse;
import com.vcall.scheduling.entity.ScheduleTemplate;
import com.vcall.scheduling.service.ScheduleTemplateService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedule-templates")
@RequiredArgsConstructor
@Tag(name = "Schedule Template Management", description = "CRUD and search for schedule templates")
public class ScheduleTemplateController {

    private final ScheduleTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Create a schedule template")
    public ResponseEntity<ApiResponse<ScheduleTemplateResponse>> createTemplate(@Valid @RequestBody ScheduleTemplateRequest request) {
        ScheduleTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Schedule template created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all schedule templates")
    public ResponseEntity<ApiResponse<Page<ScheduleTemplateResponse>>> getAllTemplates(Pageable pageable) {
        Page<ScheduleTemplateResponse> templates = templateService.getAllTemplates(pageable);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get schedule template by ID")
    public ResponseEntity<ApiResponse<ScheduleTemplateResponse>> getTemplate(@PathVariable UUID id) {
        ScheduleTemplateResponse response = templateService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Update schedule template")
    public ResponseEntity<ApiResponse<ScheduleTemplateResponse>> updateTemplate(@PathVariable UUID id,
                                                                                 @Valid @RequestBody ScheduleTemplateRequest request) {
        ScheduleTemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Schedule template updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Delete schedule template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Schedule template deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search schedule templates")
    public ResponseEntity<ApiResponse<Page<ScheduleTemplateResponse>>> searchTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID agentId,
            @RequestParam(required = false) String dayOfWeek,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        Specification<ScheduleTemplate> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
        }
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agentId"), agentId));
        }
        if (dayOfWeek != null && !dayOfWeek.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("dayOfWeek"), DayOfWeek.valueOf(dayOfWeek.toUpperCase())));
        }
        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), ScheduleTemplate.TemplateType.valueOf(type.toUpperCase())));
        }
        Page<ScheduleTemplateResponse> response = templateService.searchTemplates(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/agent/{agentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get templates by agent ID")
    public ResponseEntity<ApiResponse<List<ScheduleTemplateResponse>>> getByAgent(@PathVariable UUID agentId) {
        List<ScheduleTemplateResponse> templates = templateService.getByAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/agent/{agentId}/day/{dayOfWeek}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get templates by agent and day")
    public ResponseEntity<ApiResponse<List<ScheduleTemplateResponse>>> getByAgentAndDay(
            @PathVariable UUID agentId, @PathVariable String dayOfWeek) {
        List<ScheduleTemplateResponse> templates = templateService.getByAgentAndDay(agentId, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/agent/{agentId}/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active templates by agent")
    public ResponseEntity<ApiResponse<List<ScheduleTemplateResponse>>> getActiveByAgent(@PathVariable UUID agentId) {
        List<ScheduleTemplateResponse> templates = templateService.getActiveByAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export schedule templates to CSV")
    public void exportCsv(@RequestParam(required = false) UUID agentId,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<ScheduleTemplate> spec = Specification.where(null);
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agentId"), agentId));
        }
        Page<ScheduleTemplateResponse> items = templateService.searchTemplates(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Name", "Agent ID", "Day of Week", "Start Time", "End Time", "Type", "Active");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "name", "agentId", "dayOfWeek", "startTime", "endTime", "type", "isActive"));
        CsvExportUtil.writeCsv(response, "schedule-templates.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export schedule templates to Excel")
    public void exportExcel(@RequestParam(required = false) UUID agentId,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<ScheduleTemplate> spec = Specification.where(null);
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agentId"), agentId));
        }
        Page<ScheduleTemplateResponse> items = templateService.searchTemplates(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Name", "Agent ID", "Day of Week", "Start Time", "End Time", "Type", "Active");
        ExcelExportUtil.writeExcel(response, "schedule-templates.xlsx", headers, items.getContent(),
                Arrays.asList("id", "name", "agentId", "dayOfWeek", "startTime", "endTime", "type", "isActive"));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Bulk delete schedule templates")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkDelete(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                templateService.deleteTemplate(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }
}
