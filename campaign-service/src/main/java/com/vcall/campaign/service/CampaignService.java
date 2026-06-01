package com.vcall.campaign.service;

import com.vcall.campaign.dto.CampaignRequest;
import com.vcall.campaign.dto.CampaignResponse;
import com.vcall.campaign.dto.CampaignStatusRequest;
import com.vcall.campaign.entity.Campaign;
import com.vcall.campaign.entity.Campaign.CampaignStatus;
import com.vcall.campaign.entity.Campaign.CampaignType;
import com.vcall.campaign.entity.Campaign.DialingStrategy;
import com.vcall.campaign.entity.CampaignMember;
import com.vcall.campaign.kafka.CampaignEventPublisher;
import com.vcall.campaign.repository.CampaignMemberRepository;
import com.vcall.campaign.repository.CampaignRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository campaignMemberRepository;
    private final CampaignEventPublisher eventPublisher;

    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setType(CampaignType.valueOf(request.getType().toUpperCase()));
        campaign.setStatus(CampaignStatus.DRAFT);
        if (request.getStrategy() != null) {
            campaign.setStrategy(DialingStrategy.valueOf(request.getStrategy().toUpperCase()));
        }
        campaign.setScheduleStart(request.getScheduleStart());
        campaign.setScheduleEnd(request.getScheduleEnd());
        campaign.setTimezone(request.getTimezone());
        campaign.setCallerId(request.getCallerId());
        campaign.setDailyStart(request.getDailyStart());
        campaign.setDailyEnd(request.getDailyEnd());
        campaign.setMaxAttempts(request.getMaxAttempts());
        campaign.setRetryInterval(request.getRetryInterval());
        campaign.setAgentIdleThreshold(request.getAgentIdleThreshold());
        campaign.setIsActive(true);
        campaign = campaignRepository.save(campaign);
        return toResponse(campaign);
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        return toResponse(campaign);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getAllCampaigns() {
        return campaignRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        if (request.getType() != null) {
            campaign.setType(CampaignType.valueOf(request.getType().toUpperCase()));
        }
        if (request.getStrategy() != null) {
            campaign.setStrategy(DialingStrategy.valueOf(request.getStrategy().toUpperCase()));
        }
        campaign.setScheduleStart(request.getScheduleStart());
        campaign.setScheduleEnd(request.getScheduleEnd());
        campaign.setTimezone(request.getTimezone());
        campaign.setCallerId(request.getCallerId());
        campaign.setDailyStart(request.getDailyStart());
        campaign.setDailyEnd(request.getDailyEnd());
        campaign.setMaxAttempts(request.getMaxAttempts());
        campaign.setRetryInterval(request.getRetryInterval());
        campaign.setAgentIdleThreshold(request.getAgentIdleThreshold());
        campaign = campaignRepository.save(campaign);
        return toResponse(campaign);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        campaign.setIsActive(false);
        campaignRepository.save(campaign);
    }

    @Transactional
    public CampaignResponse startCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        campaign.setStatus(CampaignStatus.RUNNING);
        campaign = campaignRepository.save(campaign);
        eventPublisher.publishCampaignStarted(campaign);
        return toResponse(campaign);
    }

    @Transactional
    public CampaignResponse pauseCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        campaign.setStatus(CampaignStatus.PAUSED);
        campaign = campaignRepository.save(campaign);
        return toResponse(campaign);
    }

    @Transactional
    public CampaignResponse completeCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign = campaignRepository.save(campaign);
        eventPublisher.publishCampaignFinished(campaign);
        return toResponse(campaign);
    }

    @Transactional
    public CampaignResponse updateStatus(Long id, CampaignStatusRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        CampaignStatus newStatus = CampaignStatus.valueOf(request.getStatus().toUpperCase());
        campaign.setStatus(newStatus);
        campaign = campaignRepository.save(campaign);

        if (newStatus == CampaignStatus.COMPLETED || newStatus == CampaignStatus.CANCELLED) {
            eventPublisher.publishCampaignFinished(campaign);
        } else if (newStatus == CampaignStatus.RUNNING) {
            eventPublisher.publishCampaignStarted(campaign);
        }
        return toResponse(campaign);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        long totalMembers = campaignMemberRepository.countByCampaignId(id);
        long completedMembers = campaignMemberRepository.countByCampaignIdAndStatus(id, CampaignMember.MemberStatus.COMPLETED);
        long failedMembers = campaignMemberRepository.countByCampaignIdAndStatus(id, CampaignMember.MemberStatus.FAILED);
        long pendingMembers = campaignMemberRepository.countByCampaignIdAndStatus(id, CampaignMember.MemberStatus.PENDING);

        Map<String, Object> stats = new HashMap<>();
        stats.put("campaignId", id);
        stats.put("campaignName", campaign.getName());
        stats.put("status", campaign.getStatus().name());
        stats.put("totalMembers", totalMembers);
        stats.put("completedMembers", completedMembers);
        stats.put("failedMembers", failedMembers);
        stats.put("pendingMembers", pendingMembers);
        stats.put("successRate", totalMembers > 0 ? (double) completedMembers / totalMembers * 100 : 0.0);
        return stats;
    }

    private CampaignResponse toResponse(Campaign campaign) {
        long totalMembers = campaignMemberRepository.countByCampaignId(campaign.getId());
        long completedMembers = totalMembers > 0
                ? campaignMemberRepository.countByCampaignIdAndStatus(campaign.getId(), CampaignMember.MemberStatus.COMPLETED)
                : 0;
        double successRate = totalMembers > 0 ? (double) completedMembers / totalMembers * 100 : 0.0;

        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .type(campaign.getType().name())
                .status(campaign.getStatus().name())
                .strategy(campaign.getStrategy().name())
                .scheduleStart(campaign.getScheduleStart())
                .scheduleEnd(campaign.getScheduleEnd())
                .totalMembers(totalMembers)
                .completedMembers(completedMembers)
                .successRate(successRate)
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}
