package com.vcall.iam.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.iam.dto.ChangePasswordRequest;
import com.vcall.iam.dto.ProfileRequest;
import com.vcall.iam.dto.UserResponse;
import com.vcall.iam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        UserResponse response = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication, @Valid @RequestBody ProfileRequest request) {
        UserResponse response = userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/security")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecurity(Authentication authentication) {
        UserResponse user = userService.getUserByUsername(authentication.getName());
        Map<String, Object> security = Map.of(
                "email", user.getEmail(),
                "twoFactorEnabled", false,
                "lastPasswordChange", user.getCreatedAt()
        );
        return ResponseEntity.ok(ApiResponse.success(security));
    }

    @PutMapping("/security")
    public ResponseEntity<ApiResponse<Void>> updateSecurity(
            Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(),
                request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Security settings updated", null));
    }

    @GetMapping("/organization")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrganization() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "name", "VCall Contact Center",
                "timezone", "Asia/Ho_Chi_Minh",
                "language", "vi"
        )));
    }

    @PutMapping("/organization")
    public ResponseEntity<ApiResponse<Void>> updateOrganization(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success("Organization updated", null));
    }

    @GetMapping("/channels")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChannels() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "voice", true,
                "chat", true,
                "email", true,
                "sms", true
        )));
    }

    @PutMapping("/channels")
    public ResponseEntity<ApiResponse<Void>> updateChannels(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success("Channels updated", null));
    }
}
