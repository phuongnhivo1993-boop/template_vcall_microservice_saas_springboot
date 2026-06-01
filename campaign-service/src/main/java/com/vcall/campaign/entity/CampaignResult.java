package com.vcall.campaign.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "campaign_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_member_id")
    private CampaignMember campaignMember;

    @Column(name = "agent_id", columnDefinition = "UUID")
    private UUID agentId;

    @Column(name = "call_id", columnDefinition = "UUID")
    private UUID callId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 50)
    private ResultType resultType;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "disposition", length = 255)
    private String disposition;

    @Column(name = "callback_scheduled_at")
    private LocalDateTime callbackScheduledAt;

    public enum ResultType {
        ANSWERED, NO_ANSWER, BUSY, FAILED, DISCONNECTED, CALLBACK,
        APPOINTMENT_SCHEDULED, NOT_INTERESTED, DNCL
    }
}
