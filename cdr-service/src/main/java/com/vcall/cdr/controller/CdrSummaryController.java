package com.vcall.cdr.controller;

import com.vcall.cdr.dto.CdrSummaryResponse;
import com.vcall.cdr.entity.CdrSummary;
import com.vcall.cdr.repository.CdrSummaryRepository;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cdr/summary")
@RequiredArgsConstructor
public class CdrSummaryController {

    private final CdrSummaryRepository cdrSummaryRepository;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<Page<CdrSummaryResponse>>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        Page<CdrSummaryResponse> summaries = cdrSummaryRepository.findByDateBetween(startDate, endDate, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<Page<CdrSummaryResponse>>> getSummaryByTenant(
            @PathVariable UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        Page<CdrSummaryResponse> summaries = cdrSummaryRepository.findByTenantIdAndDateBetween(tenantId, startDate, endDate, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    private CdrSummaryResponse toResponse(CdrSummary summary) {
        return CdrSummaryResponse.builder()
                .date(summary.getDate())
                .totalCalls(summary.getTotalCalls())
                .answeredCalls(summary.getAnsweredCalls())
                .missedCalls(summary.getMissedCalls())
                .totalDuration(summary.getTotalDuration())
                .avgDuration(summary.getAvgDuration())
                .totalCost(summary.getTotalCost())
                .maxConcurrentCalls(summary.getMaxConcurrentCalls())
                .build();
    }
}
