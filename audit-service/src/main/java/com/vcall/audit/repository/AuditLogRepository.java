package com.vcall.audit.repository;

import com.vcall.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByActorId(UUID actorId);
    Page<AuditLog> findByActorId(UUID actorId, Pageable pageable);

    List<AuditLog> findByResourceAndResourceId(String resource, String resourceId);
    Page<AuditLog> findByResourceAndResourceId(String resource, String resourceId, Pageable pageable);

    List<AuditLog> findByAction(AuditLog.Action action);
    Page<AuditLog> findByAction(AuditLog.Action action, Pageable pageable);

    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<AuditLog> findByTimestampBefore(LocalDateTime beforeDate);

    List<AuditLog> findByStatus(AuditLog.AuditStatus status);
    Page<AuditLog> findByStatus(AuditLog.AuditStatus status, Pageable pageable);
}
