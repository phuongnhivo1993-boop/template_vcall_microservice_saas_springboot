package com.vcall.campaign.repository;

import com.vcall.campaign.entity.CampaignAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampaignAgentRepository extends JpaRepository<CampaignAgent, Long> {

    List<CampaignAgent> findByCampaignId(Long campaignId);

    Optional<CampaignAgent> findByAgentIdAndCampaignId(UUID agentId, Long campaignId);
}
