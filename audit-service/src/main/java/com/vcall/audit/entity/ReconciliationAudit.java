package com.vcall.audit.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "reconciliation_audits")
@SQLRestriction("is_deleted = false")
public class ReconciliationAudit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDateTime reconciliationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ReconciliationType type;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "matched_count")
    private Integer matchedCount;

    @Column(name = "unmatched_count")
    private Integer unmatchedCount;

    @Column(name = "discrepancy_count")
    private Integer discrepancyCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ReconciliationStatus status;

    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData;

    public enum ReconciliationType {
        CDR_BILLING, CDR_RECORDING, CALL_LOG_SIP, SMS_USAGE
    }

    public enum ReconciliationStatus {
        PENDING, COMPLETED, FAILED
    }
}
