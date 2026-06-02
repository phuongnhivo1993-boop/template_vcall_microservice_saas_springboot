package com.vcall.pbx.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.pbx.dto.ExtensionRequest;
import com.vcall.pbx.dto.ExtensionResponse;
import com.vcall.pbx.service.ExtensionService;
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

@RestController
@RequestMapping("/api/v1/pbx/extensions")
@RequiredArgsConstructor
public class ExtensionController {

    private final ExtensionService extensionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExtensionResponse>> createExtension(@Valid @RequestBody ExtensionRequest request) {
        ExtensionResponse response = extensionService.createExtension(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Extension created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExtensionResponse>>> getAllExtensions(Pageable pageable) {
        Page<ExtensionResponse> extensions = extensionService.getAllExtensions(pageable);
        return ResponseEntity.ok(ApiResponse.success(extensions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExtensionResponse>> getExtension(@PathVariable Long id) {
        ExtensionResponse response = extensionService.getExtension(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExtensionResponse>> updateExtension(@PathVariable Long id,
                                                                            @Valid @RequestBody ExtensionRequest request) {
        ExtensionResponse response = extensionService.updateExtension(id, request);
        return ResponseEntity.ok(ApiResponse.success("Extension updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExtension(@PathVariable Long id) {
        extensionService.deleteExtension(id);
        return ResponseEntity.ok(ApiResponse.success("Extension deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ExtensionResponse>> updateStatus(@PathVariable Long id,
                                                                        @RequestBody ExtensionStatusRequest request) {
        ExtensionResponse response = extensionService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    @PutMapping("/{id}/forwarding")
    public ResponseEntity<ApiResponse<ExtensionResponse>> updateForwarding(@PathVariable Long id,
                                                                            @RequestBody ExtensionForwardingRequest request) {
        ExtensionResponse response = extensionService.setCallForwarding(id, request.getCallForwarding());
        return ResponseEntity.ok(ApiResponse.success("Call forwarding updated successfully", response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<ExtensionResponse>>> getExtensionsByStatus(@PathVariable String status,
                                                                                       Pageable pageable) {
        Page<ExtensionResponse> extensions = extensionService.getByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(extensions));
    }

    @GetMapping("/sip-account/{sipAccountId}")
    public ResponseEntity<ApiResponse<Page<ExtensionResponse>>> getExtensionsBySipAccount(@PathVariable Long sipAccountId,
                                                                                           Pageable pageable) {
        Page<ExtensionResponse> extensions = extensionService.getBySipAccount(sipAccountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(extensions));
    }
}
