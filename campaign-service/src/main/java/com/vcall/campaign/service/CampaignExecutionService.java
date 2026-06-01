package com.vcall.campaign.service;

import com.vcall.campaign.entity.Campaign;
import com.vcall.campaign.entity.Campaign.CampaignStatus;
import com.vcall.campaign.entity.CampaignMember;
import com.vcall.campaign.entity.CampaignMember.MemberStatus;
import com.vcall.campaign.kafka.CampaignEventPublisher;
import com.vcall.campaign.repository.CampaignMemberRepository;
import com.vcall.campaign.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignExecutionService {

    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository campaignMemberRepository;
    private final CampaignMemberService campaignMemberService;
    private final CampaignEventPublisher eventPublisher;

    @Transactional
    public void processCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null || campaign.getStatus() != CampaignStatus.RUNNING) {
            log.warn("Campaign {} is not in RUNNING state", campaignId);
            return;
        }

        boolean hasPendingMembers = true;
        while (hasPendingMembers) {
            Optional<CampaignMember> nextMemberOpt = campaignMemberService.getNextMember(campaignId);
            if (nextMemberOpt.isEmpty()) {
                hasPendingMembers = false;
                break;
            }

            CampaignMember member = nextMemberOpt.get();
            dialMember(member);

            if (campaign.getStatus() != CampaignStatus.RUNNING) {
                break;
            }
        }

        long pendingCount = campaignMemberRepository
                .countByCampaignIdAndStatus(campaignId, MemberStatus.PENDING);
        if (pendingCount == 0) {
            campaign.setStatus(CampaignStatus.COMPLETED);
            campaignRepository.save(campaign);
            eventPublisher.publishCampaignFinished(campaign);
        }
    }

    @Transactional
    public void dialMember(CampaignMember member) {
        member.setStatus(MemberStatus.DIALING);
        member.setLastDialedAt(LocalDateTime.now());
        member.setAttempts(member.getAttempts() + 1);
        campaignMemberRepository.save(member);

        try {
            log.info("Dialing member {} with phone {}", member.getId(), member.getContactPhone());
            Thread.sleep(100);
            member.setStatus(MemberStatus.COMPLETED);
            member.setCompletedAt(LocalDateTime.now());
            member.setResult("DIALED");
            campaignMemberRepository.save(member);
            eventPublisher.publishMemberCompleted(member);
        } catch (Exception e) {
            log.error("Failed to dial member {}: {}", member.getId(), e.getMessage());
            member.setStatus(MemberStatus.FAILED);
            campaignMemberRepository.save(member);
        }
    }

    @Transactional
    public void scheduleCampaign(Long campaignId, LocalDateTime scheduleTime) {
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) {
            log.warn("Campaign {} not found for scheduling", campaignId);
            return;
        }
        campaign.setScheduleStart(scheduleTime);
        campaign.setStatus(CampaignStatus.SCHEDULED);
        campaignRepository.save(campaign);
        log.info("Campaign {} scheduled at {}", campaignId, scheduleTime);
    }

    @Transactional
    public void retryFailedMembers(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null || campaign.getStatus() != CampaignStatus.RUNNING) {
            log.warn("Campaign {} is not running, cannot retry", campaignId);
            return;
        }

        List<CampaignMember> failedMembers = campaignMemberRepository
                .findByCampaignIdAndStatus(campaignId, MemberStatus.FAILED);

        for (CampaignMember member : failedMembers) {
            if (member.getAttempts() < campaign.getMaxAttempts()) {
                member.setStatus(MemberStatus.PENDING);
                campaignMemberRepository.save(member);
                log.info("Reset member {} for retry (attempt {}/{})",
                        member.getId(), member.getAttempts(), campaign.getMaxAttempts());
            } else {
                member.setStatus(MemberStatus.SKIPPED);
                campaignMemberRepository.save(member);
                log.info("Skipping member {} - max attempts reached", member.getId());
            }
        }
    }
}
