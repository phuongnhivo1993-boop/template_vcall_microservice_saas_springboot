package com.vcall.recording.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.recording.dto.RetentionPolicyRequest;
import com.vcall.recording.dto.RetentionPolicyResponse;
import com.vcall.recording.service.RetentionPolicyService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/retention-policies")
@RequiredArgsConstructor
public class RetentionPolicyController {

    private final RetentionPolicyService retentionPolicyService;

    @PostMapping
    public ResponseEntity<ApiResponse<RetentionPolicyResponse>> createPolicy(
            @Valid @RequestBody RetentionPolicyRequest request) {
        RetentionPolicyResponse response = retentionPolicyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Retention policy created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RetentionPolicyResponse>>> getAllPolicies(Pageable pageable) {
        Page<RetentionPolicyResponse> policies = retentionPolicyService.getAllPolicies(pageable);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RetentionPolicyResponse>> getPolicy(@PathVariable Long id) {
        RetentionPolicyResponse response = retentionPolicyService.getPolicy(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RetentionPolicyResponse>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody RetentionPolicyRequest request) {
        RetentionPolicyResponse response = retentionPolicyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Retention policy updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable Long id) {
        retentionPolicyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Retention policy deleted successfully", null));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> applyRetention() {
        int affected = retentionPolicyService.applyRetention();
        return ResponseEntity.ok(ApiResponse.success(
                "Retention policy applied successfully",
                Map.of("affectedRecordings", affected)));
    }
}
