package com.vcall.cdr.repository;

import com.vcall.cdr.entity.CdrRecord;
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

    List<CdrRecord> findByCalleeNumber(String calleeNumber);

    List<CdrRecord> findByAgentId(UUID agentId);

    List<CdrRecord> findByTenantId(UUID tenantId);

    List<CdrRecord> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CdrRecord> findByStatus(CdrRecord.Status status);

    long countByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
