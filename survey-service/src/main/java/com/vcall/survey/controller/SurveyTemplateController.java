package com.vcall.survey.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.survey.dto.request.SurveyTemplateRequest;
import com.vcall.survey.dto.response.SurveyTemplateResponse;
import com.vcall.survey.entity.SurveyTemplate;
import com.vcall.survey.service.SurveyTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/survey-templates")
@RequiredArgsConstructor
@Tag(name = "Survey Template Management", description = "CRUD and search for survey templates")
public class SurveyTemplateController {

    private final SurveyTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Create a survey template")
    public ResponseEntity<ApiResponse<SurveyTemplateResponse>> createTemplate(@Valid @RequestBody SurveyTemplateRequest request) {
        SurveyTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Survey template created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all survey templates")
    public ResponseEntity<ApiResponse<Page<SurveyTemplateResponse>>> getAllTemplates(Pageable pageable) {
        Page<SurveyTemplateResponse> templates = templateService.getAllTemplates(pageable);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get survey template by ID")
    public ResponseEntity<ApiResponse<SurveyTemplateResponse>> getTemplate(@PathVariable UUID id) {
        SurveyTemplateResponse response = templateService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Update survey template")
    public ResponseEntity<ApiResponse<SurveyTemplateResponse>> updateTemplate(@PathVariable UUID id,
                                                                               @Valid @RequestBody SurveyTemplateRequest request) {
        SurveyTemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Survey template updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Delete survey template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Survey template deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search survey templates")
    public ResponseEntity<ApiResponse<Page<SurveyTemplateResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trigger,
            Pageable pageable) {
        Specification<SurveyTemplate> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
        }
        if (trigger != null && !trigger.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("trigger"), SurveyTemplate.TriggerType.valueOf(trigger.toUpperCase())));
        }
        Page<SurveyTemplateResponse> response = templateService.searchTemplates(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Bulk delete survey templates")
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
