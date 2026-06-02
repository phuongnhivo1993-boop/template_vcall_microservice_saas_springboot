package com.vcall.sipservice.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.sipservice.dto.SipRegistrationResponse;
import com.vcall.sipservice.service.SipRegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SipRegistrationResponse>>> getAll(Pageable pageable) {
        Page<SipRegistrationResponse> registrations = sipRegistrationService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SipRegistrationResponse>> getById(@PathVariable Long id) {
        SipRegistrationResponse registration = sipRegistrationService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(registration));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<SipRegistrationResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<SipRegistrationResponse> result = sipRegistrationService.search(keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("registeredAt").descending());
        Page<SipRegistrationResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = sipRegistrationService.search(keyword, null, pageable);
        } else {
            items = sipRegistrationService.findAll(pageable);
        }
        List<String> headers = Arrays.asList("ID", "SIP Account ID", "Contact URI", "User Agent", "IP Address", "Port", "Transport", "Status", "Registered At");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "sipAccountId", "contactUri", "userAgent", "ipAddress", "port", "transport", "status", "registeredAt"));
        CsvExportUtil.writeCsv(response, "sip-registrations.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("registeredAt").descending());
        Page<SipRegistrationResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = sipRegistrationService.search(keyword, null, pageable);
        } else {
            items = sipRegistrationService.findAll(pageable);
        }
        List<String> headers = Arrays.asList("ID", "SIP Account ID", "Contact URI", "User Agent", "IP Address", "Port", "Transport", "Status", "Registered At");
        ExcelExportUtil.writeExcel(response, "sip-registrations.xlsx", headers, items.getContent(),
                Arrays.asList("id", "sipAccountId", "contactUri", "userAgent", "ipAddress", "port", "transport", "status", "registeredAt"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(sipRegistrationService.getStats()));
    }
}
