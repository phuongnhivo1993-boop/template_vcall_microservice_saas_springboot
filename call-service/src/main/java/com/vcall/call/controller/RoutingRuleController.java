package com.vcall.call.controller;

import com.vcall.call.dto.RoutingRuleRequest;
import com.vcall.call.dto.RoutingRuleResponse;
import com.vcall.call.service.RoutingService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
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

@RestController
@RequestMapping("/api/v1/routing-rules")
@RequiredArgsConstructor
public class RoutingRuleController {

    private final RoutingService routingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoutingRuleResponse>> createRule(@Valid @RequestBody RoutingRuleRequest request) {
        RoutingRuleResponse response = routingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Routing rule created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RoutingRuleResponse>>> getAllRules(Pageable pageable) {
        Page<RoutingRuleResponse> rules = routingService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoutingRuleResponse>> getRule(@PathVariable Long id) {
        RoutingRuleResponse response = routingService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoutingRuleResponse>> updateRule(@PathVariable Long id,
                                                                        @Valid @RequestBody RoutingRuleRequest request) {
        RoutingRuleResponse response = routingService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Routing rule updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        routingService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Routing rule deleted", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<RoutingRuleResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        Page<RoutingRuleResponse> result = routingService.search(keyword, destination, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<RoutingRuleResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = routingService.search(keyword, null, null, pageable);
        } else {
            items = routingService.findAll(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Priority", "Condition", "Destination", "Destination ID", "Time Profile", "Active");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "name", "priority", "condition", "destination", "destinationId", "timeProfile", "isActive"));
        CsvExportUtil.writeCsv(response, "routing-rules.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<RoutingRuleResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = routingService.search(keyword, null, null, pageable);
        } else {
            items = routingService.findAll(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Priority", "Condition", "Destination", "Destination ID", "Time Profile", "Active");
        ExcelExportUtil.writeExcel(response, "routing-rules.xlsx", headers, items.getContent(),
                Arrays.asList("id", "name", "priority", "condition", "destination", "destinationId", "timeProfile", "isActive"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(routingService.getStats()));
    }
}
