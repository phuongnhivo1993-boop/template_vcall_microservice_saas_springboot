package com.vcall.sms.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.sms.dto.SmsProviderRequest;
import com.vcall.sms.dto.SmsProviderResponse;
import com.vcall.sms.service.SmsProviderService;
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
@RequestMapping("/api/v1/sms/providers")
@RequiredArgsConstructor
public class SmsProviderController {

    private final SmsProviderService smsProviderService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SmsProviderResponse>> createProvider(@Valid @RequestBody SmsProviderRequest request) {
        SmsProviderResponse response = smsProviderService.createProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Provider created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SmsProviderResponse>> getProvider(@PathVariable Long id) {
        SmsProviderResponse response = smsProviderService.getProvider(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SmsProviderResponse>>> getAllProviders(Pageable pageable) {
        Page<SmsProviderResponse> responses = smsProviderService.getAllProviders(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SmsProviderResponse>> updateProvider(
            @PathVariable Long id, @Valid @RequestBody SmsProviderRequest request) {
        SmsProviderResponse response = smsProviderService.updateProvider(id, request);
        return ResponseEntity.ok(ApiResponse.success("Provider updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteProvider(@PathVariable Long id) {
        smsProviderService.deleteProvider(id);
        return ResponseEntity.ok(ApiResponse.success("Provider deleted successfully", null));
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Boolean>> testProvider(@PathVariable Long id) {
        boolean result = smsProviderService.testProvider(id);
        return ResponseEntity.ok(ApiResponse.success("Provider test completed", result));
    }
}
