package com.vcall.crm.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.crm.dto.OpportunityRequest;
import com.vcall.crm.dto.OpportunityResponse;
import com.vcall.crm.entity.OpportunityStage;
import com.vcall.crm.service.OpportunityService;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOpportunity(@PathVariable UUID id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.ok(ApiResponse.success("Opportunity deleted successfully", null));
    }
}
