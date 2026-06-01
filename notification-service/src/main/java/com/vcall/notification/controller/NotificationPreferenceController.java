package com.vcall.notification.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.notification.dto.NotificationPreferenceRequest;
import com.vcall.notification.entity.NotificationPreference;
import com.vcall.notification.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationPreference>>> getByUser(@PathVariable UUID userId) {
        List<NotificationPreference> preferences = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(preferences));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<NotificationPreference>> update(@Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceService.updatePreference(request);
        return ResponseEntity.ok(ApiResponse.success("Preference updated", preference));
    }
}
