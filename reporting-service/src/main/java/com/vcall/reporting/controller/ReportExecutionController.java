package com.vcall.reporting.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.reporting.dto.ReportExecutionResponse;
import com.vcall.reporting.service.ReportExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportExecutionController {

    private final ReportExecutionService reportExecutionService;

    @GetMapping("/executions/{id}")
    public ResponseEntity<ApiResponse<ReportExecutionResponse>> getExecution(@PathVariable Long id) {
        ReportExecutionResponse response = reportExecutionService.getExecution(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/definitions/{id}/executions")
    public ResponseEntity<ApiResponse<List<ReportExecutionResponse>>> getExecutionHistory(
            @PathVariable Long id) {
        List<ReportExecutionResponse> responses = reportExecutionService.getExecutionHistory(id);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
