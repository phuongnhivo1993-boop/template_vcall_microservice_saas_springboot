package com.vcall.recording.repository;

import com.vcall.recording.entity.RecordingMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordingMetadataRepository extends JpaRepository<RecordingMetadata, Long> {

    List<RecordingMetadata> findByRecordingId(Long recordingId);

    List<RecordingMetadata> findByKeyAndValue(String key, String value);
}
