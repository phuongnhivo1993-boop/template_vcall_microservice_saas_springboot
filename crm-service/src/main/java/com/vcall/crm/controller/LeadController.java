package com.vcall.crm.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.BulkStatusRequest;
import com.vcall.common.dto.PagedResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.CsvUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.common.util.ExcelImportUtil;
import com.vcall.crm.dto.LeadRequest;
import com.vcall.crm.dto.LeadResponse;
import com.vcall.crm.dto.OpportunityRequest;
import com.vcall.crm.dto.OpportunityResponse;
import com.vcall.crm.entity.LeadStatus;
import com.vcall.crm.service.LeadService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/leads")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LeadController {

    private final LeadService leadService;

    @Value("${app.export.max-size:1000}")
    private int maxExportSize;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
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
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
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

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportLeadsCsv(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) LeadStatus status,
                               HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, Math.min(maxExportSize, 1000), Sort.by("createdAt").descending());
        Specification<com.vcall.crm.entity.Lead> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("firstName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("lastName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("company")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        Page<LeadResponse> leads = leadService.searchLeads(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Name", "Email", "Phone", "Company", "Status", "Score", "Source");
        List<List<String>> rows = CsvExportUtil.toRows(leads.getContent(),
                Arrays.asList("id", "fullName", "email", "phone", "company", "status", "score", "source"));
        CsvExportUtil.writeCsv(response, "leads.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportLeadsExcel(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) LeadStatus status,
                                 HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, Math.min(maxExportSize, 1000), Sort.by("createdAt").descending());
        Specification<com.vcall.crm.entity.Lead> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("firstName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("lastName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("company")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        Page<LeadResponse> leads = leadService.searchLeads(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Name", "Email", "Phone", "Company", "Status", "Score", "Source");
        ExcelExportUtil.writeExcel(response, "leads.xlsx", headers, leads.getContent(),
                Arrays.asList("id", "fullName", "email", "phone", "company", "status", "score", "source"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = leadService.getLeadStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable UUID id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.success("Lead deleted successfully", null));
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR') or hasRole('AGENT')")
    public ResponseEntity<ApiResponse<LeadResponse>> duplicateLead(@PathVariable UUID id) {
        LeadResponse response = leadService.duplicateLead(id);
        return ResponseEntity.ok(ApiResponse.success("Lead duplicated successfully", response));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<LeadResponse>> restoreLead(@PathVariable UUID id) {
        LeadResponse response = leadService.restoreLead(id);
        return ResponseEntity.ok(ApiResponse.success("Lead restored successfully", response));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkDelete(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                leadService.deleteLead(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkStatus(
            @RequestBody BulkStatusRequest request) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : request.getIds()) {
            try {
                leadService.updateLeadStatus(id, LeadStatus.valueOf(request.getStatus().toUpperCase()));
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk status update completed", result));
    }

    @PostMapping("/import/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> importLeadsCsv(
            @RequestParam("file") MultipartFile file) throws Exception {
        List<String[]> rows = CsvUtil.parseCsv(file.getInputStream());
        List<LeadResponse> importedLeads = new ArrayList<>();
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length >= 6) {
                LeadRequest request = new LeadRequest();
                request.setFirstName(row[0]);
                request.setLastName(row[1]);
                request.setEmail(row.length > 2 ? row[2] : null);
                request.setPhone(row.length > 3 ? row[3] : null);
                request.setCompany(row.length > 4 ? row[4] : null);
                request.setStatus(LeadStatus.valueOf(row[5].toUpperCase()));
                
                LeadResponse response = leadService.createLead(request);
                importedLeads.add(response);
            }
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leads imported successfully", importedLeads));
    }

    @PostMapping("/import/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> importLeadsExcel(
            @RequestParam("file") MultipartFile file) throws Exception {
        List<String[]> rows = ExcelImportUtil.parseXlsx(file.getInputStream());
        List<LeadResponse> importedLeads = new ArrayList<>();
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length >= 6) {
                LeadRequest request = new LeadRequest();
                request.setFirstName(row[0]);
                request.setLastName(row[1]);
                request.setEmail(row.length > 2 ? row[2] : null);
                request.setPhone(row.length > 3 ? row[3] : null);
                request.setCompany(row.length > 4 ? row[4] : null);
                request.setStatus(LeadStatus.valueOf(row[5].toUpperCase()));
                
                LeadResponse response = leadService.createLead(request);
                importedLeads.add(response);
            }
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leads imported successfully", importedLeads));
    }
}
