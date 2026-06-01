package com.vcall.campaign.repository;

import com.vcall.campaign.entity.CampaignMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignMemberRepository extends JpaRepository<CampaignMember, Long> {

    List<CampaignMember> findByCampaignIdAndStatus(Long campaignId, CampaignMember.MemberStatus status);

    List<CampaignMember> findByCampaignId(Long campaignId);

    long countByCampaignIdAndStatus(Long campaignId, CampaignMember.MemberStatus status);

    long countByCampaignId(Long campaignId);

    Optional<CampaignMember> findTopByCampaignIdAndStatusOrderByPriorityAscLastDialedAtAsc(
            Long campaignId, CampaignMember.MemberStatus status);
}
