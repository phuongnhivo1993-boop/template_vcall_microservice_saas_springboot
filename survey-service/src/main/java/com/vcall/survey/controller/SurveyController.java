package com.vcall.survey.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.survey.dto.request.SurveyRequest;
import com.vcall.survey.dto.response.SurveyResponse;
import com.vcall.survey.entity.Survey;
import com.vcall.survey.service.SurveyService;
import com.vcall.survey.service.SurveyResponseService;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey Management", description = "CRUD and search for surveys")
public class SurveyController {

    private final SurveyService surveyService;
    private final SurveyResponseService surveyResponseService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Create a survey")
    public ResponseEntity<ApiResponse<SurveyResponse>> createSurvey(@Valid @RequestBody SurveyRequest request) {
        SurveyResponse response = surveyService.createSurvey(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Survey created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all surveys")
    public ResponseEntity<ApiResponse<Page<SurveyResponse>>> getAllSurveys(Pageable pageable) {
        Page<SurveyResponse> surveys = surveyService.getAllSurveys(pageable);
        return ResponseEntity.ok(ApiResponse.success(surveys));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get survey by ID")
    public ResponseEntity<ApiResponse<SurveyResponse>> getSurvey(@PathVariable UUID id) {
        SurveyResponse response = surveyService.getSurvey(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Update survey")
    public ResponseEntity<ApiResponse<SurveyResponse>> updateSurvey(@PathVariable UUID id,
                                                                     @Valid @RequestBody SurveyRequest request) {
        SurveyResponse response = surveyService.updateSurvey(id, request);
        return ResponseEntity.ok(ApiResponse.success("Survey updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Delete survey")
    public ResponseEntity<ApiResponse<Void>> deleteSurvey(@PathVariable UUID id) {
        surveyService.deleteSurvey(id);
        return ResponseEntity.ok(ApiResponse.success("Survey deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search surveys")
    public ResponseEntity<ApiResponse<Page<SurveyResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        Specification<Survey> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), Survey.SurveyType.valueOf(type.toUpperCase())));
        }
        Page<SurveyResponse> response = surveyService.searchSurveys(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get survey statistics (CSAT/NPS)")
    public ResponseEntity<ApiResponse<com.vcall.survey.dto.response.SurveyStatsResponse>> getStats(
            @PathVariable UUID id) {
        var stats = surveyResponseService.getStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Export surveys to CSV")
    public void exportCsv(HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SurveyResponse> items = surveyService.getAllSurveys(pageable);
        List<String> headers = Arrays.asList("ID", "Title", "Description", "Type", "Active", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "title", "description", "type", "isActive", "createdAt"));
        CsvExportUtil.writeCsv(response, "surveys.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Export surveys to Excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SurveyResponse> items = surveyService.getAllSurveys(pageable);
        List<String> headers = Arrays.asList("ID", "Title", "Description", "Type", "Active", "Created At");
        ExcelExportUtil.writeExcel(response, "surveys.xlsx", headers, items.getContent(),
                Arrays.asList("id", "title", "description", "type", "isActive", "createdAt"));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Bulk delete surveys")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkDelete(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                surveyService.deleteSurvey(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }
}
