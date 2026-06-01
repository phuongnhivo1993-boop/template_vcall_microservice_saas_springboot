package com.vcall.crm.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.crm.dto.ActivityRequest;
import com.vcall.crm.dto.ActivityResponse;
import com.vcall.crm.service.ActivityService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(@Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.createActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Activity created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(@PathVariable Long id) {
        ActivityResponse response = activityService.getActivity(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getAllActivities(
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        List<ActivityResponse> responses;
        if (assignedTo != null && startDate != null && endDate != null) {
            responses = activityService.getActivitiesByDateRange(assignedTo, startDate, endDate);
        } else {
            responses = activityService.getAllActivities();
        }
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivitiesByCustomer(@PathVariable UUID customerId) {
        List<ActivityResponse> responses = activityService.getActivitiesByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.updateActivity(id, request);
        return ResponseEntity.ok(ApiResponse.success("Activity updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.ok(ApiResponse.success("Activity deleted successfully", null));
    }
}
