package com.vcall.audit.repository;

import com.vcall.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByActorId(UUID actorId);

    List<AuditLog> findByResourceAndResourceId(String resource, String resourceId);

    List<AuditLog> findByAction(AuditLog.Action action);

    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByStatus(AuditLog.AuditStatus status);
}
