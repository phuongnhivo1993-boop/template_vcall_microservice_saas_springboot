package com.vcall.campaign.controller;

import com.vcall.campaign.dto.CampaignResultResponse;
import com.vcall.campaign.service.CampaignResultService;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns/{campaignId}/results")
@RequiredArgsConstructor
public class CampaignResultController {

    private final CampaignResultService campaignResultService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CampaignResultResponse>>> getResults(
            @PathVariable Long campaignId, Pageable pageable) {
        Page<CampaignResultResponse> results = campaignResultService.getResults(campaignId, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<ApiResponse<Page<CampaignResultResponse>>> getAgentResults(
            @PathVariable Long campaignId,
            @PathVariable UUID agentId, Pageable pageable) {
        Page<CampaignResultResponse> results = campaignResultService.getAgentResults(agentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
