package com.vcall.cdr.repository;

import com.vcall.cdr.entity.CdrRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CdrRecordRepository extends JpaRepository<CdrRecord, UUID>, JpaSpecificationExecutor<CdrRecord> {

    Optional<CdrRecord> findByCallId(String callId);

    List<CdrRecord> findByCallerNumber(String callerNumber);
    Page<CdrRecord> findByCallerNumber(String callerNumber, Pageable pageable);

    List<CdrRecord> findByCalleeNumber(String calleeNumber);
    Page<CdrRecord> findByCalleeNumber(String calleeNumber, Pageable pageable);

    List<CdrRecord> findByAgentId(UUID agentId);
    Page<CdrRecord> findByAgentId(UUID agentId, Pageable pageable);

    List<CdrRecord> findByTenantId(UUID tenantId);
    Page<CdrRecord> findByTenantId(UUID tenantId, Pageable pageable);

    List<CdrRecord> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    Page<CdrRecord> findByStartTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<CdrRecord> findByStatus(CdrRecord.Status status);
    Page<CdrRecord> findByStatus(CdrRecord.Status status, Pageable pageable);

    long countByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
