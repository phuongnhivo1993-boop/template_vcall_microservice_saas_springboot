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

@Entity
@Table(name = "campaign_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "contact_name", length = 255)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_data", columnDefinition = "TEXT")
    private String contactData;

    @Column(name = "priority")
    private Integer priority = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MemberStatus status = MemberStatus.PENDING;

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "last_dialed_at")
    private LocalDateTime lastDialedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum MemberStatus {
        PENDING, DIALING, IN_PROGRESS, COMPLETED, FAILED, NO_ANSWER, BUSY, SKIPPED
    }
}
