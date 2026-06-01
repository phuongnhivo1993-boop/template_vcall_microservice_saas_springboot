package com.vcall.recording.repository;

import com.vcall.recording.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecordingRepository extends JpaRepository<Recording, UUID>, JpaSpecificationExecutor<Recording> {

    List<Recording> findByCallId(UUID callId);

    List<Recording> findByAgentId(UUID agentId);

    List<Recording> findByStatus(Recording.RecordingStatus status);

    List<Recording> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Recording> findByCompletedAtBefore(LocalDateTime dateTime);
}
