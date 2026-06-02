package com.vcall.campaign.controller;

import com.vcall.campaign.dto.CampaignRequest;
import com.vcall.campaign.dto.CampaignResponse;
import com.vcall.campaign.dto.CampaignStatusRequest;
import com.vcall.campaign.entity.Campaign;
import com.vcall.campaign.service.CampaignService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @Valid @RequestBody CampaignRequest request) {
        CampaignResponse response = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Campaign created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> getAllCampaigns(Pageable pageable) {
        Page<CampaignResponse> campaigns = campaignService.getAllCampaigns(pageable);
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaign(@PathVariable Long id) {
        CampaignResponse response = campaignService.getCampaign(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaign(@PathVariable Long id,
                                                                         @Valid @RequestBody CampaignRequest request) {
        CampaignResponse response = campaignService.updateCampaign(id, request);
        return ResponseEntity.ok(ApiResponse.success("Campaign updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign deleted successfully", null));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<CampaignResponse>> startCampaign(@PathVariable Long id) {
        CampaignResponse response = campaignService.startCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign started successfully", response));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<CampaignResponse>> pauseCampaign(@PathVariable Long id) {
        CampaignResponse response = campaignService.pauseCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign paused successfully", response));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<CampaignResponse>> stopCampaign(@PathVariable Long id) {
        CampaignResponse response = campaignService.completeCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign stopped successfully", response));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateStatus(@PathVariable Long id,
                                                                       @Valid @RequestBody CampaignStatusRequest request) {
        CampaignResponse response = campaignService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCampaignStats(@PathVariable Long id) {
        Map<String, Object> stats = campaignService.getStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> searchCampaigns(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        Specification<Campaign> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("status").as(String.class)), status.toLowerCase()));
        }
        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("type").as(String.class)), type.toLowerCase()));
        }
        Page<CampaignResponse> result = campaignService.searchCampaigns(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    public void exportCampaignsCsv(@RequestParam(required = false) String keyword,
                                    HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<CampaignResponse> campaigns;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<Campaign> spec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
            campaigns = campaignService.searchCampaigns(spec, pageable);
        } else {
            campaigns = campaignService.getAllCampaigns(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Type", "Status", "Strategy", "Start", "End", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(campaigns.getContent(),
                Arrays.asList("id", "name", "type", "status", "strategy", "scheduleStart", "scheduleEnd", "createdAt"));
        CsvExportUtil.writeCsv(response, "campaigns.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<CampaignResponse> campaigns;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<Campaign> spec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
            campaigns = campaignService.searchCampaigns(spec, pageable);
        } else {
            campaigns = campaignService.getAllCampaigns(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Type", "Status", "Strategy", "Start", "End", "Created At");
        ExcelExportUtil.writeExcel(response, "campaigns.xlsx", headers, campaigns.getContent(),
                Arrays.asList("id", "name", "type", "status", "strategy", "scheduleStart", "scheduleEnd", "createdAt"));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<Long>>> bulkDelete(
            @RequestBody List<Long> ids) {
        BulkOperationUtil.BulkResult<Long> result = new BulkOperationUtil.BulkResult<>();
        for (Long id : ids) {
            try {
                campaignService.deleteCampaign(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<Long>>> bulkStatus(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        List<Long> ids = rawIds.stream().map(Integer::longValue).collect(Collectors.toList());
        String status = (String) body.get("status");
        CampaignStatusRequest statusRequest = new CampaignStatusRequest(status);
        BulkOperationUtil.BulkResult<Long> result = new BulkOperationUtil.BulkResult<>();
        for (Long id : ids) {
            try {
                campaignService.updateStatus(id, statusRequest);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk status update completed", result));
    }
}
