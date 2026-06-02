package com.vcall.crm.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.crm.dto.OpportunityRequest;
import com.vcall.crm.dto.OpportunityResponse;
import com.vcall.crm.entity.OpportunityStage;
import com.vcall.crm.service.OpportunityService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    @PostMapping
    public ResponseEntity<ApiResponse<OpportunityResponse>> createOpportunity(@Valid @RequestBody OpportunityRequest request) {
        OpportunityResponse response = opportunityService.createOpportunity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Opportunity created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityResponse>> getOpportunity(@PathVariable UUID id) {
        OpportunityResponse response = opportunityService.getOpportunity(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OpportunityResponse>>> getAllOpportunities(
            @RequestParam(required = false) UUID leadId,
            @RequestParam(required = false) OpportunityStage stage,
            Pageable pageable) {
        Page<OpportunityResponse> responses;
        if (leadId != null) {
            responses = opportunityService.getOpportunitiesByLeadId(leadId, pageable);
        } else if (stage != null) {
            responses = opportunityService.getOpportunitiesByStage(stage, pageable);
        } else {
            responses = opportunityService.getAllOpportunities(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityResponse>> updateOpportunity(@PathVariable UUID id, @Valid @RequestBody OpportunityRequest request) {
        OpportunityResponse response = opportunityService.updateOpportunity(id, request);
        return ResponseEntity.ok(ApiResponse.success("Opportunity updated successfully", response));
    }

    @PatchMapping("/{id}/stage")
    public ResponseEntity<ApiResponse<OpportunityResponse>> updateStage(@PathVariable UUID id, @RequestBody OpportunityStage stage) {
        OpportunityResponse response = opportunityService.updateStage(id, stage);
        return ResponseEntity.ok(ApiResponse.success("Opportunity stage updated successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<OpportunityResponse>>> searchOpportunities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OpportunityStage stage,
            Pageable pageable) {
        Specification<com.vcall.crm.entity.Opportunity> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (stage != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("stage"), stage));
        }
        Page<OpportunityResponse> response = opportunityService.searchOpportunities(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    public void exportOpportunitiesCsv(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) OpportunityStage stage,
                                       HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<com.vcall.crm.entity.Opportunity> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (stage != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("stage"), stage));
        }
        Page<OpportunityResponse> opportunities = opportunityService.searchOpportunities(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Title", "Stage", "Value", "Currency", "Probability", "Expected Close", "Assigned To");
        List<List<String>> rows = CsvExportUtil.toRows(opportunities.getContent(),
                Arrays.asList("id", "title", "stage", "value", "currency", "probability", "expectedCloseDate", "assignedTo"));
        CsvExportUtil.writeCsv(response, "opportunities.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportOpportunitiesExcel(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) OpportunityStage stage,
                                         HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<com.vcall.crm.entity.Opportunity> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (stage != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("stage"), stage));
        }
        Page<OpportunityResponse> opportunities = opportunityService.searchOpportunities(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Title", "Stage", "Value", "Currency", "Probability", "Expected Close", "Assigned To");
        ExcelExportUtil.writeExcel(response, "opportunities.xlsx", headers, opportunities.getContent(),
                Arrays.asList("id", "title", "stage", "value", "currency", "probability", "expectedCloseDate", "assignedTo"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = opportunityService.getOpportunityStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOpportunity(@PathVariable UUID id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.ok(ApiResponse.success("Opportunity deleted successfully", null));
    }
}
