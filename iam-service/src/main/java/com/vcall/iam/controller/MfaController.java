package com.vcall.iam.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.iam.dto.MfaSetupResponse;
import com.vcall.iam.dto.MfaVerifyRequest;
import com.vcall.iam.entity.User;
import com.vcall.iam.repository.UserRepository;
import com.vcall.iam.service.MfaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;
    private final UserRepository userRepository;

    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MfaSetupResponse>> setup(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        MfaSetupResponse response = mfaService.generateSetup(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> verify(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody MfaVerifyRequest request) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean verified = mfaService.enableMfa(user.getId(), request.getSecret(), request.getCode());
        if (verified) {
            return ResponseEntity.ok(ApiResponse.success("MFA enabled successfully", "verified"));
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Invalid verification code"));
    }

    @PostMapping("/disable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> disable(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        mfaService.disableMfa(user.getId());
        return ResponseEntity.ok(ApiResponse.success("MFA disabled", "disabled"));
    }
}
