package com.vcall.campaign.service;

import com.vcall.campaign.dto.CampaignResultResponse;
import com.vcall.campaign.entity.Campaign;
import com.vcall.campaign.entity.CampaignMember;
import com.vcall.campaign.entity.CampaignResult;
import com.vcall.campaign.entity.CampaignResult.ResultType;
import com.vcall.campaign.kafka.CampaignEventPublisher;
import com.vcall.campaign.repository.CampaignMemberRepository;
import com.vcall.campaign.repository.CampaignRepository;
import com.vcall.campaign.repository.CampaignResultRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignResultService {

    private final CampaignResultRepository campaignResultRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository campaignMemberRepository;
    private final CampaignEventPublisher eventPublisher;

    @Transactional
    public CampaignResultResponse recordResult(Long campaignId, Long memberId, UUID agentId,
                                                UUID callId, String resultType, Integer duration,
                                                String notes, String disposition) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        CampaignMember member = null;
        if (memberId != null) {
            member = campaignMemberRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign member not found with id: " + memberId));
            member.setStatus(CampaignMember.MemberStatus.COMPLETED);
            member.setResult(resultType);
            member.setCompletedAt(java.time.LocalDateTime.now());
            campaignMemberRepository.save(member);
            eventPublisher.publishMemberCompleted(member);
        }

        CampaignResult result = new CampaignResult();
        result.setCampaign(campaign);
        result.setCampaignMember(member);
        result.setAgentId(agentId);
        result.setCallId(callId);
        result.setResultType(ResultType.valueOf(resultType.toUpperCase()));
        result.setDuration(duration);
        result.setNotes(notes);
        result.setDisposition(disposition);
        result = campaignResultRepository.save(result);

        return toResponse(result);
    }

    @Transactional(readOnly = true)
    public List<CampaignResultResponse> getResults(Long campaignId) {
        return campaignResultRepository.findByCampaignId(campaignId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CampaignResultResponse> getAgentResults(UUID agentId) {
        return campaignResultRepository.findByAgentId(agentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CampaignResultResponse> getResultsByType(String resultType) {
        ResultType type = ResultType.valueOf(resultType.toUpperCase());
        return campaignResultRepository.findByResultType(type).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CampaignResultResponse toResponse(CampaignResult result) {
        return CampaignResultResponse.builder()
                .id(result.getId())
                .campaignId(result.getCampaign().getId())
                .agentId(result.getAgentId())
                .callId(result.getCallId())
                .resultType(result.getResultType().name())
                .duration(result.getDuration())
                .notes(result.getNotes())
                .disposition(result.getDisposition())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
