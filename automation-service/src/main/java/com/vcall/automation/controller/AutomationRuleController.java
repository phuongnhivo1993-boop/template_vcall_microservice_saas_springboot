package com.vcall.automation.controller;

import com.vcall.automation.dto.AutomationRuleRequest;
import com.vcall.automation.dto.AutomationRuleResponse;
import com.vcall.automation.service.AutomationRuleService;
import com.vcall.common.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/automation/rules")
public class AutomationRuleController {

    private final AutomationRuleService automationRuleService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<AutomationRuleResponse>>> getAllRules(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        Page<AutomationRuleResponse> page = automationRuleService.getAllRules(name, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> getRule(@PathVariable Long id) {
        AutomationRuleResponse rule = automationRuleService.getRule(id);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> createRule(
            @Valid @RequestBody AutomationRuleRequest request) {
        AutomationRuleResponse rule = automationRuleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rule created successfully", rule));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody AutomationRuleRequest request) {
        AutomationRuleResponse rule = automationRuleService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Rule updated successfully", rule));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        automationRuleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("Rule deleted successfully", null));
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> duplicateRule(@PathVariable Long id) {
        AutomationRuleResponse response = automationRuleService.duplicateRule(id);
        return ResponseEntity.ok(ApiResponse.success("Rule duplicated successfully", response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> toggleRule(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean isActive = body.getOrDefault("isActive", false);
        AutomationRuleResponse rule = automationRuleService.toggleRule(id, isActive);
        return ResponseEntity.ok(ApiResponse.success("Rule toggled successfully", rule));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteRules(@RequestBody List<Long> ids) {
        automationRuleService.bulkDeleteRules(ids);
        return ResponseEntity.ok(ApiResponse.success("Rules deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<AutomationRuleResponse>>> searchRules(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String trigger,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        Page<AutomationRuleResponse> result = automationRuleService.searchRules(name, trigger, action, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRuleStats() {
        Map<String, Object> stats = automationRuleService.getRuleStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
