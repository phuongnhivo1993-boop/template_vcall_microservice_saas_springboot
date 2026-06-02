package com.vcall.sms.repository;

import com.vcall.sms.entity.SmsCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsCampaignRepository extends JpaRepository<SmsCampaign, Long> {

    List<SmsCampaign> findByStatus(SmsCampaign.CampaignStatus status);
    Page<SmsCampaign> findByStatus(SmsCampaign.CampaignStatus status, Pageable pageable);

    List<SmsCampaign> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
}
