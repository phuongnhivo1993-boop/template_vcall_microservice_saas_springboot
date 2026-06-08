package com.vcall.sipservice.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.sipservice.dto.SipDeviceRequest;
import com.vcall.sipservice.dto.SipDeviceResponse;
import com.vcall.sipservice.service.SipDeviceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/sip/devices")
@RequiredArgsConstructor
public class SipDeviceController {

    private final SipDeviceService sipDeviceService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SipDeviceResponse>>> getAll(Pageable pageable) {
        Page<SipDeviceResponse> devices = sipDeviceService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SipDeviceResponse>> getById(@PathVariable Long id) {
        SipDeviceResponse device = sipDeviceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(device));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SipDeviceResponse>> create(@Valid @RequestBody SipDeviceRequest request,
                                                                 @RequestParam Long sipAccountId) {
        SipDeviceResponse device = sipDeviceService.create(request, sipAccountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("SipDevice created", device));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SipDeviceResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody SipDeviceRequest request) {
        SipDeviceResponse device = sipDeviceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("SipDevice updated", device));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        sipDeviceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("SipDevice deleted", null));
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SipDeviceResponse>>> getByAccountId(@PathVariable Long accountId,
                                                                                 Pageable pageable) {
        Page<SipDeviceResponse> devices = sipDeviceService.findByAccountId(accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SipDeviceResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deviceType,
            Pageable pageable) {
        Page<SipDeviceResponse> result = sipDeviceService.search(keyword, deviceType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SipDeviceResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = sipDeviceService.search(keyword, null, pageable);
        } else {
            items = sipDeviceService.findAll(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Device Type", "User Agent", "IP Address", "MAC Address", "Firmware");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "name", "deviceType", "userAgent", "ipAddress", "macAddress", "firmwareVersion"));
        CsvExportUtil.writeCsv(response, "sip-devices.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SipDeviceResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = sipDeviceService.search(keyword, null, pageable);
        } else {
            items = sipDeviceService.findAll(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Device Type", "User Agent", "IP Address", "MAC Address", "Firmware");
        ExcelExportUtil.writeExcel(response, "sip-devices.xlsx", headers, items.getContent(),
                Arrays.asList("id", "name", "deviceType", "userAgent", "ipAddress", "macAddress", "firmwareVersion"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(sipDeviceService.getStats()));
    }
}
