package com.vcall.ticket.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.ticket.dto.SlaRuleRequest;
import com.vcall.ticket.dto.SlaRuleResponse;
import com.vcall.ticket.service.SlaService;
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
@RequestMapping("/api/v1/sla-rules")
@RequiredArgsConstructor
public class SlaRuleController {

    private final SlaService slaService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SlaRuleResponse>> createRule(@Valid @RequestBody SlaRuleRequest request) {
        SlaRuleResponse response = slaService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SLA rule created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SlaRuleResponse>>> getAllRules(Pageable pageable) {
        Page<SlaRuleResponse> rules = slaService.getAllRules(pageable);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SlaRuleResponse>> getRule(@PathVariable Long id) {
        SlaRuleResponse response = slaService.getRule(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SlaRuleResponse>> updateRule(@PathVariable Long id,
                                                                    @Valid @RequestBody SlaRuleRequest request) {
        SlaRuleResponse response = slaService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.success("SLA rule updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        slaService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("SLA rule deleted successfully", null));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<SlaRuleResponse>>> getActiveRules(Pageable pageable) {
        Page<SlaRuleResponse> rules = slaService.getActiveRules(pageable);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @PostMapping("/check")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<String>> checkSla() {
        slaService.monitorSla();
        return ResponseEntity.ok(ApiResponse.success("SLA check completed"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<SlaRuleResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        Page<SlaRuleResponse> result = slaService.search(keyword, priority, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportCsv(@RequestParam(required = false) String keyword,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SlaRuleResponse> items = slaService.search(keyword, null, null, pageable);
        List<String> headers = Arrays.asList("ID", "Name", "Priority", "Category", "First Response Time", "Resolution Time", "Escalation Level", "Active");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "name", "priority", "category", "firstResponseTime", "resolutionTime", "escalationLevel", "isActive"));
        CsvExportUtil.writeCsv(response, "sla-rules.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SlaRuleResponse> items = slaService.search(keyword, null, null, pageable);
        List<String> headers = Arrays.asList("ID", "Name", "Priority", "Category", "First Response Time", "Resolution Time", "Escalation Level", "Active");
        ExcelExportUtil.writeExcel(response, "sla-rules.xlsx", headers, items.getContent(),
                Arrays.asList("id", "name", "priority", "category", "firstResponseTime", "resolutionTime", "escalationLevel", "isActive"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(slaService.getStats()));
    }
}
