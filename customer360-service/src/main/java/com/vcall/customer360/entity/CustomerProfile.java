package com.vcall.customer360.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "customer_profiles")
public class CustomerProfile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "total_calls", columnDefinition = "integer default 0")
    private Integer totalCalls;

    @Column(name = "total_tickets", columnDefinition = "integer default 0")
    private Integer totalTickets;

    @Column(name = "total_leads", columnDefinition = "integer default 0")
    private Integer totalLeads;

    @Column(name = "total_opportunities", columnDefinition = "integer default 0")
    private Integer totalOpportunities;

    @Column(name = "total_spent", columnDefinition = "decimal(15,2) default 0")
    private java.math.BigDecimal totalSpent;

    @Column(name = "last_contact_at")
    private java.time.LocalDateTime lastContactAt;

    @Column(name = "lifetime_value", columnDefinition = "decimal(15,2) default 0")
    private java.math.BigDecimal lifetimeValue;

    @Column(name = "satisfaction_score", columnDefinition = "decimal(3,1)")
    private java.math.BigDecimal satisfactionScore;

    @Column(name = "segment", length = 50)
    private String segment;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
