package com.vcall.campaign.service;

import com.vcall.campaign.dto.CampaignAgentRequest;
import com.vcall.campaign.dto.CampaignAgentResponse;
import com.vcall.campaign.entity.Campaign;
import com.vcall.campaign.entity.CampaignAgent;
import com.vcall.campaign.repository.CampaignAgentRepository;
import com.vcall.campaign.repository.CampaignRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignAgentService {

    private final CampaignAgentRepository campaignAgentRepository;
    private final CampaignRepository campaignRepository;

    @Transactional
    public CampaignAgentResponse assignAgent(Long campaignId, CampaignAgentRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        CampaignAgent agent = new CampaignAgent();
        agent.setCampaign(campaign);
        agent.setAgentId(request.getAgentId());
        agent.setMaxConcurrentCalls(request.getMaxConcurrentCalls() != null ? request.getMaxConcurrentCalls() : 5);
        agent.setIsActive(true);
        agent = campaignAgentRepository.save(agent);
        return toResponse(agent);
    }

    @Transactional
    public void removeAgent(Long campaignId, UUID agentId) {
        CampaignAgent agent = campaignAgentRepository.findByAgentIdAndCampaignId(agentId, campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign agent not found"));
        campaignAgentRepository.delete(agent);
    }

    @Transactional(readOnly = true)
    public List<CampaignAgentResponse> getAgents(Long campaignId) {
        return campaignAgentRepository.findByCampaignId(campaignId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CampaignAgentResponse toResponse(CampaignAgent agent) {
        return CampaignAgentResponse.builder()
                .id(agent.getId())
                .campaignId(agent.getCampaign().getId())
                .agentId(agent.getAgentId())
                .maxConcurrentCalls(agent.getMaxConcurrentCalls())
                .isActive(agent.getIsActive())
                .build();
    }
}
