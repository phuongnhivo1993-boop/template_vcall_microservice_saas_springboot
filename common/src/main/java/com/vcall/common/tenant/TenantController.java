package com.vcall.common.tenant;

import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantProvisioningService provisioningService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TenantRegistrationResponse>> register(
            @Valid @RequestBody TenantRegistrationRequest request) {
        TenantRegistrationResponse response = provisioningService.provisionTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant registered successfully", response));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @RequestParam String companyName) {
        boolean available = provisioningService.isTenantAvailable(companyName);
        return ResponseEntity.ok(ApiResponse.success(available));
    }
}
