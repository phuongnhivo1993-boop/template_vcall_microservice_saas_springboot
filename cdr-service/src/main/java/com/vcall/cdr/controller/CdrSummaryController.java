package com.vcall.cdr.controller;

import com.vcall.cdr.dto.CdrSummaryResponse;
import com.vcall.cdr.entity.CdrSummary;
import com.vcall.cdr.repository.CdrSummaryRepository;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cdr/summary")
@RequiredArgsConstructor
public class CdrSummaryController {

    private final CdrSummaryRepository cdrSummaryRepository;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<CdrSummaryResponse>>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CdrSummaryResponse> summaries = cdrSummaryRepository.findByDateBetween(startDate, endDate).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<CdrSummaryResponse>>> getSummaryByTenant(
            @PathVariable UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CdrSummaryResponse> summaries = cdrSummaryRepository.findByDateBetween(startDate, endDate).stream()
                .filter(s -> tenantId.equals(s.getTenantId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
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
