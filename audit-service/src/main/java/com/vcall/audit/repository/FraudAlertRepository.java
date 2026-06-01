package com.vcall.audit.repository;

import com.vcall.audit.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID>, JpaSpecificationExecutor<FraudAlert> {

    List<FraudAlert> findByStatus(FraudAlert.AlertStatus status);

    List<FraudAlert> findBySeverity(FraudAlert.Severity severity);

    List<FraudAlert> findByDetectedAtBetween(LocalDateTime start, LocalDateTime end);

    List<FraudAlert> findByActorId(UUID actorId);
}
