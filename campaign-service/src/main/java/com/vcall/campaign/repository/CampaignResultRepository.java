package com.vcall.campaign.repository;

import com.vcall.campaign.entity.CampaignResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignResultRepository extends JpaRepository<CampaignResult, Long> {

    List<CampaignResult> findByCampaignId(Long campaignId);

    List<CampaignResult> findByAgentId(UUID agentId);

    List<CampaignResult> findByResultType(CampaignResult.ResultType resultType);

    List<CampaignResult> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
