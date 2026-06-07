package com.vcall.campaign.repository;

import com.vcall.campaign.entity.Campaign;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {

    @EntityGraph(attributePaths = {"members"})
    List<Campaign> findByStatus(Campaign.CampaignStatus status);

    @EntityGraph(attributePaths = {"members"})
    List<Campaign> findByIsActiveTrue();

    List<Campaign> findByScheduleStartBetween(LocalDateTime start, LocalDateTime end);

    List<Campaign> findByType(Campaign.CampaignType type);
}
