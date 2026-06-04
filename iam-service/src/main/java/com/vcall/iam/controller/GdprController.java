package com.vcall.iam.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.gdpr.GdprService;
import com.vcall.iam.dto.UserResponse;
import com.vcall.iam.entity.User;
import com.vcall.iam.repository.UserRepository;
import com.vcall.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gdpr")
@RequiredArgsConstructor
public class GdprController {

    private final GdprService gdprService;
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/export")
    public ResponseEntity<ApiResponse<UserResponse>> exportMyData(Authentication authentication) {
        UserResponse response = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Data export requested. Check your email.", response));
    }

    @GetMapping(value = "/export/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportMyDataJson(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String json = gdprService.exportUserDataAsJson(user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=gdpr-export.json")
                .body(json);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMyData(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        gdprService.anonymizePiiFields(user);
        user.setUsername("deleted-" + user.getId());
        user.setEmail(null);
        user.setPhone(null);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Your data has been anonymized per GDPR request", null));
    }

    @PostMapping("/anonymize/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> anonymizeUser(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        gdprService.anonymizePiiFields(user);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User data anonymized successfully", null));
    }
}
