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

@Entity
@Table(name = "report_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "schedule", length = 200)
    private String schedule;

    @Column(name = "recipients", columnDefinition = "TEXT")
    private String recipients;

    @Column(name = "is_active")
    private boolean isActive = true;

    public enum ReportType {
        CALL_VOLUME, AGENT_PERFORMANCE, SLA_COMPLIANCE, COST_ANALYSIS, CDR_SUMMARY, BILLING_SUMMARY
    }
}
