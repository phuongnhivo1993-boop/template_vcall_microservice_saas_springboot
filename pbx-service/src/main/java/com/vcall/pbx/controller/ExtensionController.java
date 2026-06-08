package com.vcall.pbx.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.pbx.dto.ExtensionRequest;
import com.vcall.pbx.dto.ExtensionResponse;
import com.vcall.pbx.service.ExtensionService;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/pbx/extensions")
@RequiredArgsConstructor
public class ExtensionController {

    private final ExtensionService extensionService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ExtensionResponse>> createExtension(@Valid @RequestBody ExtensionRequest request) {
        ExtensionResponse response = extensionService.createExtension(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Extension created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ExtensionResponse>>> getAllExtensions(Pageable pageable) {
        Page<ExtensionResponse> extensions = extensionService.getAllExtensions(pageable);
        return ResponseEntity.ok(ApiResponse.success(extensions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ExtensionResponse>> getExtension(@PathVariable Long id) {
        ExtensionResponse response = extensionService.getExtension(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ExtensionResponse>> updateExtension(@PathVariable Long id,
                                                                            @Valid @RequestBody ExtensionRequest request) {
        ExtensionResponse response = extensionService.updateExtension(id, request);
        return ResponseEntity.ok(ApiResponse.success("Extension updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteExtension(@PathVariable Long id) {
        extensionService.deleteExtension(id);
        return ResponseEntity.ok(ApiResponse.success("Extension deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ExtensionResponse>> updateStatus(@PathVariable Long id,
                                                                        @RequestBody ExtensionStatusRequest request) {
        ExtensionResponse response = extensionService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    @PutMapping("/{id}/forwarding")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ExtensionResponse>> updateForwarding(@PathVariable Long id,
                                                                            @RequestBody ExtensionForwardingRequest request) {
        ExtensionResponse response = extensionService.setCallForwarding(id, request.getCallForwarding());
        return ResponseEntity.ok(ApiResponse.success("Call forwarding updated successfully", response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ExtensionResponse>>> getExtensionsByStatus(@PathVariable String status,
                                                                                       Pageable pageable) {
        Page<ExtensionResponse> extensions = extensionService.getByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(extensions));
    }

    @GetMapping("/sip-account/{sipAccountId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ExtensionResponse>>> getExtensionsBySipAccount(@PathVariable Long sipAccountId,
                                                                                            Pageable pageable) {
        Page<ExtensionResponse> extensions = extensionService.getBySipAccount(sipAccountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(extensions));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<ExtensionResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        Page<ExtensionResponse> result = extensionService.search(keyword, status, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<ExtensionResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = extensionService.search(keyword, null, null, pageable);
        } else {
            items = extensionService.getAllExtensions(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Extension Number", "Display Name", "Type", "Status", "Voicemail", "Outbound CID", "Max Calls", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "extensionNumber", "displayName", "type", "status", "voicemailEnabled", "outboundCallerId", "maxConcurrentCalls", "createdAt"));
        CsvExportUtil.writeCsv(response, "extensions.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<ExtensionResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = extensionService.search(keyword, null, null, pageable);
        } else {
            items = extensionService.getAllExtensions(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Extension Number", "Display Name", "Type", "Status", "Voicemail", "Outbound CID", "Max Calls", "Created At");
        ExcelExportUtil.writeExcel(response, "extensions.xlsx", headers, items.getContent(),
                Arrays.asList("id", "extensionNumber", "displayName", "type", "status", "voicemailEnabled", "outboundCallerId", "maxConcurrentCalls", "createdAt"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(extensionService.getStats()));
    }
}
