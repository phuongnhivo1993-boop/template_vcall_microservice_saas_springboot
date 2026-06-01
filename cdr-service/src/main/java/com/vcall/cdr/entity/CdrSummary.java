package com.vcall.cdr.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cdr_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CdrSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "tenant_id", columnDefinition = "UUID", nullable = false)
    private UUID tenantId;

    @Column(name = "total_calls")
    private Long totalCalls;

    @Column(name = "answered_calls")
    private Long answeredCalls;

    @Column(name = "missed_calls")
    private Long missedCalls;

    @Column(name = "total_duration")
    private Long totalDuration;

    @Column(name = "avg_duration")
    private Double avgDuration;

    @Column(name = "total_cost", precision = 14, scale = 4)
    private BigDecimal totalCost;

    @Column(name = "max_concurrent_calls")
    private Integer maxConcurrentCalls;
}
