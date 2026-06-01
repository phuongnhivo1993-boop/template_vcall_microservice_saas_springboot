package com.vcall.campaign.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "campaign_agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignAgent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "agent_id", columnDefinition = "UUID", nullable = false)
    private UUID agentId;

    @Column(name = "max_concurrent_calls")
    private Integer maxConcurrentCalls = 5;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
