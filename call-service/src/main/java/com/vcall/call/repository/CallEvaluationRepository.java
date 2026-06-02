package com.vcall.call.repository;

import com.vcall.call.entity.CallEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CallEvaluationRepository extends JpaRepository<CallEvaluation, UUID> {
    Page<CallEvaluation> findByCallId(UUID callId, Pageable pageable);
    Page<CallEvaluation> findByEvaluatorId(UUID evaluatorId, Pageable pageable);
}
