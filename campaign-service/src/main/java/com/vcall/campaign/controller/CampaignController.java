package com.vcall.campaign.controller;

import com.vcall.campaign.dto.CampaignRequest;
import com.vcall.campaign.dto.CampaignResponse;
import com.vcall.campaign.dto.CampaignStatusRequest;
import com.vcall.campaign.service.CampaignService;
import com.vcall.common.dto.ApiResponse;
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

import java.util.Map;

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
}
