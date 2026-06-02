package com.vcall.billing.controller;

import com.vcall.billing.dto.UsageRecordRequest;
import com.vcall.billing.dto.UsageRecordResponse;
import com.vcall.billing.entity.UsageRecord;
import com.vcall.billing.service.UsageRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing/usage")
@RequiredArgsConstructor
public class UsageRecordController {

    private final UsageRecordService usageRecordService;

    @PostMapping
    public ResponseEntity<UsageRecordResponse> recordUsage(@Valid @RequestBody UsageRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usageRecordService.recordUsage(request));
    }

    @GetMapping("/{subscriberId}")
    public ResponseEntity<Page<UsageRecordResponse>> getUsage(@PathVariable UUID subscriberId,
                                                               Pageable pageable) {
        return ResponseEntity.ok(usageRecordService.getUsageBySubscriber(subscriberId, pageable));
    }

    @GetMapping("/{subscriberId}/summary")
    public ResponseEntity<UsageRecordService.UsageSummary> getUsageSummary(
            @PathVariable UUID subscriberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(usageRecordService.aggregateUsage(subscriberId, startDate, endDate));
    }

    @GetMapping("/{subscriberId}/type/{usageType}")
    public ResponseEntity<Page<UsageRecordResponse>> getUsageByTypeAndPeriod(
            @PathVariable UUID subscriberId,
            @PathVariable UsageRecord.UsageType usageType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(usageRecordService.getUsageByTypeAndPeriod(subscriberId, usageType, startDate, endDate, pageable));
    }
}
