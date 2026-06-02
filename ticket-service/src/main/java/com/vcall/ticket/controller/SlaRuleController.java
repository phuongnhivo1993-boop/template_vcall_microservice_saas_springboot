package com.vcall.ticket.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.ticket.dto.SlaRuleRequest;
import com.vcall.ticket.dto.SlaRuleResponse;
import com.vcall.ticket.service.SlaService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sla-rules")
@RequiredArgsConstructor
public class SlaRuleController {

    private final SlaService slaService;

    @PostMapping
    public ResponseEntity<ApiResponse<SlaRuleResponse>> createRule(@Valid @RequestBody SlaRuleRequest request) {
        SlaRuleResponse response = slaService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SLA rule created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SlaRuleResponse>>> getAllRules(Pageable pageable) {
        Page<SlaRuleResponse> rules = slaService.getAllRules(pageable);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SlaRuleResponse>> getRule(@PathVariable Long id) {
        SlaRuleResponse response = slaService.getRule(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SlaRuleResponse>> updateRule(@PathVariable Long id,
                                                                    @Valid @RequestBody SlaRuleRequest request) {
        SlaRuleResponse response = slaService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.success("SLA rule updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        slaService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("SLA rule deleted successfully", null));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<SlaRuleResponse>>> getActiveRules(Pageable pageable) {
        Page<SlaRuleResponse> rules = slaService.getActiveRules(pageable);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<String>> checkSla() {
        slaService.monitorSla();
        return ResponseEntity.ok(ApiResponse.success("SLA check completed"));
    }
}
