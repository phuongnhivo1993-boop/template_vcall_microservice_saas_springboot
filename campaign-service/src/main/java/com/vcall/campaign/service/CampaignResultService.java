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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public Page<CampaignResultResponse> getResults(Long campaignId, Pageable pageable) {
        return campaignResultRepository.findByCampaignId(campaignId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResultResponse> getAgentResults(UUID agentId, Pageable pageable) {
        return campaignResultRepository.findByAgentId(agentId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CampaignResultResponse> getResultsByType(String resultType) {
        ResultType type = ResultType.valueOf(resultType.toUpperCase());
        return campaignResultRepository.findByResultType(type).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CampaignResultResponse> search(Long campaignId, String keyword, String resultType, Pageable pageable) {
        Specification<CampaignResult> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("campaign").get("id"), campaignId));
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("disposition")), pattern),
                        cb.like(cb.lower(root.get("notes")), pattern)
                ));
            }
            if (resultType != null && !resultType.isEmpty()) {
                predicates.add(cb.equal(root.get("resultType"), ResultType.valueOf(resultType.toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return campaignResultRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public void deleteResult(Long resultId) {
        CampaignResult result = campaignResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign result not found with id: " + resultId));
        campaignResultRepository.delete(result);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats(Long campaignId) {
        List<CampaignResult> results = campaignResultRepository.findByCampaignId(campaignId);
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) results.size());
        for (ResultType t : ResultType.values()) {
            stats.put(t.name().toLowerCase(), results.stream().filter(r -> r.getResultType() == t).count());
        }
        return stats;
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
