package com.vcall.audit.repository;

import com.vcall.audit.entity.SecurityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, UUID>, JpaSpecificationExecutor<SecurityLog> {

    List<SecurityLog> findByEventType(SecurityLog.EventType eventType);
    Page<SecurityLog> findByEventType(SecurityLog.EventType eventType, Pageable pageable);

    List<SecurityLog> findByActorId(UUID actorId);
    Page<SecurityLog> findByActorId(UUID actorId, Pageable pageable);

    List<SecurityLog> findByIpAddress(String ipAddress);
    Page<SecurityLog> findByIpAddress(String ipAddress, Pageable pageable);

    List<SecurityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    Page<SecurityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<SecurityLog> findByIsSuspiciousTrue();
    Page<SecurityLog> findByIsSuspiciousTrue(Pageable pageable);

    List<SecurityLog> findByRiskLevel(SecurityLog.RiskLevel riskLevel);
    Page<SecurityLog> findByRiskLevel(SecurityLog.RiskLevel riskLevel, Pageable pageable);
}
