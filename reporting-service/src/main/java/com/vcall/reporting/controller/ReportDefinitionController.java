package com.vcall.reporting.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.reporting.dto.ReportDefinitionRequest;
import com.vcall.reporting.dto.ReportDefinitionResponse;
import com.vcall.reporting.service.ReportDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports/definitions")
@RequiredArgsConstructor
public class ReportDefinitionController {

    private final ReportDefinitionService reportDefinitionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportDefinitionResponse>> createReport(
            @Valid @RequestBody ReportDefinitionRequest request) {
        ReportDefinitionResponse response = reportDefinitionService.createReport(request);
        return ResponseEntity.ok(ApiResponse.success("Report definition created", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDefinitionResponse>> updateReport(
            @PathVariable Long id, @Valid @RequestBody ReportDefinitionRequest request) {
        ReportDefinitionResponse response = reportDefinitionService.updateReport(id, request);
        return ResponseEntity.ok(ApiResponse.success("Report definition updated", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDefinitionResponse>> getReport(@PathVariable Long id) {
        ReportDefinitionResponse response = reportDefinitionService.getReport(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReportDefinitionResponse>>> getAllReports(
            @RequestParam(required = false) String reportType,
            Pageable pageable) {
        Page<ReportDefinitionResponse> responses;
        if (reportType != null && !reportType.isBlank()) {
            responses = reportDefinitionService.getReportsByType(reportType, pageable);
        } else {
            responses = reportDefinitionService.getAllReports(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        reportDefinitionService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report definition deleted", null));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiResponse<Void>> executeReport(@PathVariable Long id) {
        reportDefinitionService.executeReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report execution started", null));
    }
}
