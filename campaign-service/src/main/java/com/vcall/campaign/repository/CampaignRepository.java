package com.vcall.campaign.repository;

import com.vcall.campaign.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {

    List<Campaign> findByStatus(Campaign.CampaignStatus status);

    List<Campaign> findByIsActiveTrue();

    List<Campaign> findByScheduleStartBetween(LocalDateTime start, LocalDateTime end);

    List<Campaign> findByType(Campaign.CampaignType type);
}
