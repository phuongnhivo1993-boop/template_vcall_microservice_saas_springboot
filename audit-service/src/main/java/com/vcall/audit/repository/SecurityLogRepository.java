package com.vcall.audit.repository;

import com.vcall.audit.entity.SecurityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, UUID>, JpaSpecificationExecutor<SecurityLog> {

    List<SecurityLog> findByEventType(SecurityLog.EventType eventType);

    List<SecurityLog> findByActorId(UUID actorId);

    List<SecurityLog> findByIpAddress(String ipAddress);

    List<SecurityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<SecurityLog> findByIsSuspiciousTrue();

    List<SecurityLog> findByRiskLevel(SecurityLog.RiskLevel riskLevel);
}
