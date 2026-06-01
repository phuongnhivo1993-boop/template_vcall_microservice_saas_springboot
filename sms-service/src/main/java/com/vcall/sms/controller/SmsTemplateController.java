package com.vcall.sms.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.sms.dto.SmsTemplateRequest;
import com.vcall.sms.dto.SmsTemplateResponse;
import com.vcall.sms.service.SmsTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/sms/templates")
@RequiredArgsConstructor
public class SmsTemplateController {

    private final SmsTemplateService smsTemplateService;

    @PostMapping
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> createTemplate(@Valid @RequestBody SmsTemplateRequest request) {
        SmsTemplateResponse response = smsTemplateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> getTemplate(@PathVariable Long id) {
        SmsTemplateResponse response = smsTemplateService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SmsTemplateResponse>>> getAllTemplates() {
        List<SmsTemplateResponse> responses = smsTemplateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> updateTemplate(
            @PathVariable Long id, @Valid @RequestBody SmsTemplateRequest request) {
        SmsTemplateResponse response = smsTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        smsTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully", null));
    }
}
