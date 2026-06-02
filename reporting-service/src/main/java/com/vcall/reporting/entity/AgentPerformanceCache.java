package com.vcall.reporting.entity;

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

import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "agent_performance_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class AgentPerformanceCache extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", columnDefinition = "UUID", nullable = false)
    private UUID agentId;

    @Column(name = "agent_name", nullable = false, length = 200)
    private String agentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 10)
    private Period period;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "total_calls")
    private Long totalCalls = 0L;

    @Column(name = "answered_calls")
    private Long answeredCalls = 0L;

    @Column(name = "missed_calls")
    private Long missedCalls = 0L;

    @Column(name = "avg_talk_duration")
    private Double avgTalkDuration = 0.0;

    @Column(name = "avg_wait_duration")
    private Double avgWaitDuration = 0.0;

    @Column(name = "total_talk_time")
    private Long totalTalkTime = 0L;

    @Column(name = "max_concurrent_calls")
    private Integer maxConcurrentCalls = 0;

    @Column(name = "satisfaction_score")
    private Double satisfactionScore = 0.0;

    @Column(name = "occupancy_rate")
    private Double occupancyRate = 0.0;

    public enum Period {
        DAILY, WEEKLY, MONTHLY
    }
}
