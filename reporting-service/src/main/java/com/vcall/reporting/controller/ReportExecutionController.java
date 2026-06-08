package com.vcall.reporting.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.reporting.dto.ReportExecutionResponse;
import com.vcall.reporting.service.PdfExportService;
import com.vcall.reporting.service.ReportExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportExecutionController {

    private final ReportExecutionService reportExecutionService;
    private final PdfExportService pdfExportService;

    @GetMapping("/executions/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ReportExecutionResponse>> getExecution(@PathVariable Long id) {
        ReportExecutionResponse response = reportExecutionService.getExecution(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/definitions/{id}/executions")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ReportExecutionResponse>>> getExecutionHistory(
            @PathVariable Long id,
            Pageable pageable) {
        Page<ReportExecutionResponse> responses = reportExecutionService.getExecutionHistory(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long id) {
        ReportExecutionResponse response = reportExecutionService.getExecution(id);
        Resource pdfResource = pdfExportService.exportToPdf(
                reportExecutionService.getExecutionEntity(id),
                reportExecutionService.getExecutionData(id));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + id + ".pdf\"")
                .body(pdfResource);
    }
}
