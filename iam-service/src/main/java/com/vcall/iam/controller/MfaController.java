package com.vcall.iam.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.iam.dto.MfaSetupResponse;
import com.vcall.iam.dto.MfaVerifyRequest;
import com.vcall.iam.service.MfaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;

    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MfaSetupResponse>> setup(@AuthenticationPrincipal UUID userId) {
        MfaSetupResponse response = mfaService.generateSetup(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> verify(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody MfaVerifyRequest request) {
        boolean verified = mfaService.enableMfa(userId, request.getSecret(), request.getCode());
        if (verified) {
            return ResponseEntity.ok(ApiResponse.success("MFA enabled successfully", "verified"));
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Invalid verification code"));
    }

    @PostMapping("/disable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> disable(@AuthenticationPrincipal UUID userId) {
        mfaService.disableMfa(userId);
        return ResponseEntity.ok(ApiResponse.success("MFA disabled", "disabled"));
    }
}
