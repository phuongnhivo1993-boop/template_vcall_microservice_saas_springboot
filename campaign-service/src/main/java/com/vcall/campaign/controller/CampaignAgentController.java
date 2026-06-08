package com.vcall.campaign.controller;

import com.vcall.campaign.dto.CampaignAgentRequest;
import com.vcall.campaign.dto.CampaignAgentResponse;
import com.vcall.campaign.service.CampaignAgentService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns/{campaignId}/agents")
@RequiredArgsConstructor
public class CampaignAgentController {

    private final CampaignAgentService campaignAgentService;

    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<CampaignAgentResponse>> assignAgent(
            @PathVariable Long campaignId,
            @Valid @RequestBody CampaignAgentRequest request) {
        CampaignAgentResponse response = campaignAgentService.assignAgent(campaignId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agent assigned to campaign successfully", response));
    }

    @DeleteMapping("/{agentId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> removeAgent(
            @PathVariable Long campaignId,
            @PathVariable UUID agentId) {
        campaignAgentService.removeAgent(campaignId, agentId);
        return ResponseEntity.ok(ApiResponse.success("Agent removed from campaign successfully", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<CampaignAgentResponse>>> getAgents(
            @PathVariable Long campaignId) {
        List<CampaignAgentResponse> agents = campaignAgentService.getAgents(campaignId);
        return ResponseEntity.ok(ApiResponse.success(agents));
    }
}
