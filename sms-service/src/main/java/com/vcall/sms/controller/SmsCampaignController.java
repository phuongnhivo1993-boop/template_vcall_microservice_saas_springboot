package com.vcall.sms.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.sms.dto.SmsCampaignRequest;
import com.vcall.sms.dto.SmsCampaignResponse;
import com.vcall.sms.service.SmsCampaignService;
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
@RequestMapping("/api/v1/sms/campaigns")
@RequiredArgsConstructor
public class SmsCampaignController {

    private final SmsCampaignService smsCampaignService;

    @PostMapping
    public ResponseEntity<ApiResponse<SmsCampaignResponse>> createCampaign(@Valid @RequestBody SmsCampaignRequest request) {
        SmsCampaignResponse response = smsCampaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Campaign created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SmsCampaignResponse>> getCampaign(@PathVariable Long id) {
        SmsCampaignResponse response = smsCampaignService.getCampaign(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SmsCampaignResponse>>> getAllCampaigns(Pageable pageable) {
        Page<SmsCampaignResponse> responses = smsCampaignService.getAllCampaigns(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SmsCampaignResponse>> updateCampaign(
            @PathVariable Long id, @Valid @RequestBody SmsCampaignRequest request) {
        SmsCampaignResponse response = smsCampaignService.updateCampaign(id, request);
        return ResponseEntity.ok(ApiResponse.success("Campaign updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Long id) {
        smsCampaignService.deleteCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign deleted successfully", null));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<SmsCampaignResponse>> startCampaign(@PathVariable Long id) {
        SmsCampaignResponse response = smsCampaignService.startCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign started successfully", response));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<SmsCampaignResponse>> pauseCampaign(@PathVariable Long id) {
        SmsCampaignResponse response = smsCampaignService.pauseCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign paused successfully", response));
    }
}
