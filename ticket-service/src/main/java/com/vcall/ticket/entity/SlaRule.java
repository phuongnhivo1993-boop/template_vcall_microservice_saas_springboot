package com.vcall.ticket.entity;

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

@Entity
@Table(name = "sla_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlaRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Ticket.TicketPriority priority;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "first_response_time")
    private Integer firstResponseTime;

    @Column(name = "resolution_time")
    private Integer resolutionTime;

    @Column(name = "escalation_level")
    private Integer escalationLevel;

    @Column(name = "escalation_notify_to", columnDefinition = "TEXT")
    private String escalationNotifyTo;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
