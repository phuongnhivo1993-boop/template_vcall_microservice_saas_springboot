package com.vcall.campaign.controller;

import com.vcall.campaign.dto.CampaignResultResponse;
import com.vcall.campaign.service.CampaignResultService;
import com.vcall.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns/{campaignId}/results")
@RequiredArgsConstructor
public class CampaignResultController {

    private final CampaignResultService campaignResultService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignResultResponse>>> getResults(
            @PathVariable Long campaignId) {
        List<CampaignResultResponse> results = campaignResultService.getResults(campaignId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<ApiResponse<List<CampaignResultResponse>>> getAgentResults(
            @PathVariable Long campaignId,
            @PathVariable UUID agentId) {
        List<CampaignResultResponse> results = campaignResultService.getAgentResults(agentId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
