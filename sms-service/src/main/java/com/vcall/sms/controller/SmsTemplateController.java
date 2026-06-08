package com.vcall.sms.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.sms.dto.SmsTemplateRequest;
import com.vcall.sms.dto.SmsTemplateResponse;
import com.vcall.sms.service.SmsTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/sms/templates")
@RequiredArgsConstructor
public class SmsTemplateController {

    private final SmsTemplateService smsTemplateService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> createTemplate(@Valid @RequestBody SmsTemplateRequest request) {
        SmsTemplateResponse response = smsTemplateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> getTemplate(@PathVariable Long id) {
        SmsTemplateResponse response = smsTemplateService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SmsTemplateResponse>>> getAllTemplates(Pageable pageable) {
        Page<SmsTemplateResponse> responses = smsTemplateService.getAllTemplates(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> updateTemplate(
            @PathVariable Long id, @Valid @RequestBody SmsTemplateRequest request) {
        SmsTemplateResponse response = smsTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        smsTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully", null));
    }
}
