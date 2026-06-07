package com.vcall.call.repository;

import com.vcall.call.entity.CallStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CallStatusHistoryRepository extends JpaRepository<CallStatusHistory, UUID> {

    List<CallStatusHistory> findByCallIdOrderByChangedAtAsc(UUID callId);
}
