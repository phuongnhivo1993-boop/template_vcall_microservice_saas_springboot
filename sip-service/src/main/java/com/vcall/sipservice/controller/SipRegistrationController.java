package com.vcall.sipservice.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.sipservice.dto.SipRegistrationResponse;
import com.vcall.sipservice.service.SipRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sip/registrations")
@RequiredArgsConstructor
public class SipRegistrationController {

    private final SipRegistrationService sipRegistrationService;

    @PostMapping
    public ResponseEntity<ApiResponse<SipRegistrationResponse>> register(@RequestBody Map<String, Object> body) {
        Long sipAccountId = Long.valueOf(body.get("sipAccountId").toString());
        String contactUri = (String) body.get("contactUri");
        String userAgent = (String) body.get("userAgent");
        String ipAddress = (String) body.get("ipAddress");
        Integer port = body.get("port") != null ? Integer.valueOf(body.get("port").toString()) : null;
        String transport = (String) body.get("transport");
        Integer expires = body.get("expires") != null ? Integer.valueOf(body.get("expires").toString()) : null;

        SipRegistrationResponse registration = sipRegistrationService.register(
                sipAccountId, contactUri, userAgent, ipAddress, port, transport, expires);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SipRegistration created", registration));
    }

    @PutMapping("/{id}/refresh")
    public ResponseEntity<ApiResponse<SipRegistrationResponse>> refresh(@PathVariable Long id) {
        SipRegistrationResponse registration = sipRegistrationService.refresh(id);
        return ResponseEntity.ok(ApiResponse.success("SipRegistration refreshed", registration));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> unregister(@PathVariable Long id) {
        sipRegistrationService.unregister(id);
        return ResponseEntity.ok(ApiResponse.success("SipRegistration unregistered", null));
    }
}
