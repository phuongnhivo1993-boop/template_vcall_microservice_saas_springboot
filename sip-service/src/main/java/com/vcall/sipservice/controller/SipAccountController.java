package com.vcall.sipservice.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.sipservice.dto.SipAccountRequest;
import com.vcall.sipservice.dto.SipAccountResponse;
import com.vcall.sipservice.service.SipAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sip/accounts")
@RequiredArgsConstructor
public class SipAccountController {

    private final SipAccountService sipAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SipAccountResponse>>> getAll(Pageable pageable) {
        Page<SipAccountResponse> accounts = sipAccountService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SipAccountResponse>> getById(@PathVariable Long id) {
        SipAccountResponse account = sipAccountService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SipAccountResponse>> create(@Valid @RequestBody SipAccountRequest request) {
        SipAccountResponse account = sipAccountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("SipAccount created", account));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SipAccountResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody SipAccountRequest request) {
        SipAccountResponse account = sipAccountService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("SipAccount updated", account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        sipAccountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("SipAccount deleted", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SipAccountResponse>> updateStatus(@PathVariable Long id,
                                                                        @RequestBody Map<String, String> body) {
        String status = body.get("status");
        SipAccountResponse account = sipAccountService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("SipAccount status updated", account));
    }
}
