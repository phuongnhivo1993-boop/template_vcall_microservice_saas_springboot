package com.vcall.call.repository;

import com.vcall.call.entity.Call;
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
public interface CallRepository extends JpaRepository<Call, UUID>, JpaSpecificationExecutor<Call> {

    Optional<Call> findByCallId(String callId);

    List<Call> findByAgentIdAndStatus(UUID agentId, Call.CallStatus status);

    Page<Call> findByAgentIdAndStatus(UUID agentId, Call.CallStatus status, Pageable pageable);

    List<Call> findByStatusAndStartTimeBetween(Call.CallStatus status, LocalDateTime start, LocalDateTime end);

    Page<Call> findByStatusAndStartTimeBetween(Call.CallStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);

    long countByStatus(Call.CallStatus status);
}
