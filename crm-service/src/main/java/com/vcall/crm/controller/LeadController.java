package com.vcall.crm.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.PagedResponse;
import com.vcall.crm.dto.LeadRequest;
import com.vcall.crm.dto.LeadResponse;
import com.vcall.crm.dto.OpportunityRequest;
import com.vcall.crm.dto.OpportunityResponse;
import com.vcall.crm.entity.LeadStatus;
import com.vcall.crm.service.LeadService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody LeadRequest request) {
        LeadResponse response = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Lead created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> getLead(@PathVariable UUID id) {
        LeadResponse response = leadService.getLead(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LeadResponse>>> searchLeads(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<com.vcall.crm.entity.Lead> spec = Specification.where(null);
        if (firstName != null) spec = spec.and((root, query, cb) -> cb.like(root.get("firstName"), "%" + firstName + "%"));
        if (lastName != null) spec = spec.and((root, query, cb) -> cb.like(root.get("lastName"), "%" + lastName + "%"));
        if (email != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("email"), email));
        if (company != null) spec = spec.and((root, query, cb) -> cb.like(root.get("company"), "%" + company + "%"));
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));

        Page<LeadResponse> leads = leadService.searchLeads(spec, pageable);
        PagedResponse<LeadResponse> paged = new PagedResponse<>(
                leads.getContent(), leads.getNumber(), leads.getSize(),
                leads.getTotalElements(), leads.getTotalPages(), leads.isLast());
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(@PathVariable UUID id, @Valid @RequestBody LeadRequest request) {
        LeadResponse response = leadService.updateLead(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lead updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLeadStatus(@PathVariable UUID id, @RequestBody LeadStatus status) {
        LeadResponse response = leadService.updateLeadStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Lead status updated successfully", response));
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<ApiResponse<OpportunityResponse>> convertLead(@PathVariable UUID id, @Valid @RequestBody OpportunityRequest request) {
        OpportunityResponse response = leadService.convertLeadToOpportunity(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Lead converted to opportunity successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable UUID id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.success("Lead deleted successfully", null));
    }
}
