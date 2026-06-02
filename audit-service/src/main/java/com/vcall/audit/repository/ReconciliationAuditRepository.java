package com.vcall.audit.repository;

import com.vcall.audit.entity.ReconciliationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReconciliationAuditRepository extends JpaRepository<ReconciliationAudit, UUID>,
        JpaSpecificationExecutor<ReconciliationAudit> {

    List<ReconciliationAudit> findByTypeAndStatus(ReconciliationAudit.ReconciliationType type,
                                                   ReconciliationAudit.ReconciliationStatus status);
    Page<ReconciliationAudit> findByTypeAndStatus(ReconciliationAudit.ReconciliationType type,
                                                   ReconciliationAudit.ReconciliationStatus status, Pageable pageable);

    List<ReconciliationAudit> findByReconciliationDateBetween(LocalDateTime start, LocalDateTime end);
    Page<ReconciliationAudit> findByReconciliationDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
