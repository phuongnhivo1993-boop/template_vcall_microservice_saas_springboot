package com.vcall.sipservice.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.sipservice.dto.SipDeviceRequest;
import com.vcall.sipservice.dto.SipDeviceResponse;
import com.vcall.sipservice.service.SipDeviceService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/sip/devices")
@RequiredArgsConstructor
public class SipDeviceController {

    private final SipDeviceService sipDeviceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SipDeviceResponse>>> getAll() {
        List<SipDeviceResponse> devices = sipDeviceService.findAll();
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SipDeviceResponse>> getById(@PathVariable Long id) {
        SipDeviceResponse device = sipDeviceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(device));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SipDeviceResponse>> create(@Valid @RequestBody SipDeviceRequest request,
                                                                 @RequestParam Long sipAccountId) {
        SipDeviceResponse device = sipDeviceService.create(request, sipAccountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("SipDevice created", device));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SipDeviceResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody SipDeviceRequest request) {
        SipDeviceResponse device = sipDeviceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("SipDevice updated", device));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        sipDeviceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("SipDevice deleted", null));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<SipDeviceResponse>>> getByAccountId(@PathVariable Long accountId) {
        List<SipDeviceResponse> devices = sipDeviceService.findByAccountId(accountId);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }
}
