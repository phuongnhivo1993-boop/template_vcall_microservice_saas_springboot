package com.vcall.campaign.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Campaign extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy", nullable = false, length = 50)
    private DialingStrategy strategy = DialingStrategy.SEQUENTIAL;

    @Column(name = "schedule_start")
    private LocalDateTime scheduleStart;

    @Column(name = "schedule_end")
    private LocalDateTime scheduleEnd;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "caller_id", length = 50)
    private String callerId;

    @Column(name = "daily_start")
    private LocalTime dailyStart;

    @Column(name = "daily_end")
    private LocalTime dailyEnd;

    @Column(name = "max_attempts")
    private Integer maxAttempts = 3;

    @Column(name = "retry_interval")
    private Integer retryInterval;

    @Column(name = "agent_idle_threshold")
    private Integer agentIdleThreshold;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum CampaignType {
        OUTBOUND_CALL, SMS, EMAIL, PREDICTIVE, PREVIEW
    }

    public enum CampaignStatus {
        DRAFT, SCHEDULED, RUNNING, PAUSED, COMPLETED, CANCELLED
    }

    public enum DialingStrategy {
        SEQUENTIAL, RANDOM, PRIORITY
    }
}
