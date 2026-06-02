package com.vcall.sms.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.sms.dto.SmsRequest;
import com.vcall.sms.dto.SmsResponse;
import com.vcall.sms.service.SmsService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<SmsResponse>> sendSms(@Valid @RequestBody SmsRequest request) {
        SmsResponse response = smsService.sendSms(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SMS sent successfully", response));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<SmsResponse>>> sendBatchSms(@Valid @RequestBody List<SmsRequest> requests) {
        List<SmsResponse> responses = smsService.sendBatchSms(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch SMS sent successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SmsResponse>> getSms(@PathVariable UUID id) {
        SmsResponse response = smsService.getSmsStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SmsResponse>>> getAllSms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<SmsResponse> responses = smsService.getSmsHistory(null, null, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<SmsResponse>>> getSmsHistory(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<SmsResponse> responses = smsService.getSmsHistory(from, to, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
