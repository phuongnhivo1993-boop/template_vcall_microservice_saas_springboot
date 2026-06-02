package com.vcall.audit.repository;

import com.vcall.audit.entity.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID>, JpaSpecificationExecutor<FraudAlert> {

    List<FraudAlert> findByStatus(FraudAlert.AlertStatus status);
    Page<FraudAlert> findByStatus(FraudAlert.AlertStatus status, Pageable pageable);

    List<FraudAlert> findBySeverity(FraudAlert.Severity severity);
    Page<FraudAlert> findBySeverity(FraudAlert.Severity severity, Pageable pageable);

    List<FraudAlert> findByDetectedAtBetween(LocalDateTime start, LocalDateTime end);
    Page<FraudAlert> findByDetectedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<FraudAlert> findByActorId(UUID actorId);
    Page<FraudAlert> findByActorId(UUID actorId, Pageable pageable);
}
