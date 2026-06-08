package com.vcall.sipservice.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.sipservice.dto.SipAccountRequest;
import com.vcall.sipservice.dto.SipAccountResponse;
import com.vcall.sipservice.service.SipAccountService;
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
@RequestMapping("/api/v1/sip/accounts")
@RequiredArgsConstructor
public class SipAccountController {

    private final SipAccountService sipAccountService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SipAccountResponse>>> getAll(Pageable pageable) {
        Page<SipAccountResponse> accounts = sipAccountService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SipAccountResponse>> getById(@PathVariable Long id) {
        SipAccountResponse account = sipAccountService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SipAccountResponse>> create(@Valid @RequestBody SipAccountRequest request) {
        SipAccountResponse account = sipAccountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("SipAccount created", account));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SipAccountResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody SipAccountRequest request) {
        SipAccountResponse account = sipAccountService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("SipAccount updated", account));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        sipAccountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("SipAccount deleted", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SipAccountResponse>> updateStatus(@PathVariable Long id,
                                                                         @RequestBody Map<String, String> body) {
        String status = body.get("status");
        SipAccountResponse account = sipAccountService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("SipAccount status updated", account));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SipAccountResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountType,
            Pageable pageable) {
        Page<SipAccountResponse> result = sipAccountService.search(keyword, status, accountType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SipAccountResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = sipAccountService.search(keyword, null, null, pageable);
        } else {
            items = sipAccountService.findAll(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Username", "Domain", "Realm", "Account Type", "Status", "Max Channels", "Tenant ID");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "username", "domain", "realm", "accountType", "status", "maxChannels", "tenantId"));
        CsvExportUtil.writeCsv(response, "sip-accounts.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SipAccountResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = sipAccountService.search(keyword, null, null, pageable);
        } else {
            items = sipAccountService.findAll(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Username", "Domain", "Realm", "Account Type", "Status", "Max Channels", "Tenant ID");
        ExcelExportUtil.writeExcel(response, "sip-accounts.xlsx", headers, items.getContent(),
                Arrays.asList("id", "username", "domain", "realm", "accountType", "status", "maxChannels", "tenantId"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(sipAccountService.getStats()));
    }
}
