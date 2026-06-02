package com.vcall.notification.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.notification.dto.PushDeviceRequest;
import com.vcall.notification.dto.PushDeviceResponse;
import com.vcall.notification.service.PushDeviceService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/push/devices")
@RequiredArgsConstructor
public class PushDeviceController {

    private final PushDeviceService pushDeviceService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PushDeviceResponse>> register(@Valid @RequestBody PushDeviceRequest request) {
        PushDeviceResponse response = pushDeviceService.registerDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Device registered", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> unregister(@PathVariable Long id) {
        pushDeviceService.unregisterDevice(id);
        return ResponseEntity.ok(ApiResponse.success("Device unregistered", null));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<PushDeviceResponse>>> getByUser(@PathVariable UUID userId, Pageable pageable) {
        Page<PushDeviceResponse> devices = pushDeviceService.getActiveDevices(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }
}
