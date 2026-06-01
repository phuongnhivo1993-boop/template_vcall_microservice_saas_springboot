package com.vcall.ticket.entity;

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
@Table(name = "sla_breaches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlaBreach extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_rule_id", nullable = false)
    private SlaRule slaRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "breach_type", nullable = false, length = 20)
    private BreachType breachType;

    @Column(name = "breached_at", nullable = false)
    private LocalDateTime breachedAt;

    @Column(name = "notified")
    private Boolean notified = false;

    public enum BreachType {
        FIRST_RESPONSE, RESOLUTION
    }
}
