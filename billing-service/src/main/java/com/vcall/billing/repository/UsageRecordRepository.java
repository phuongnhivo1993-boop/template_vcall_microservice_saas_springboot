package com.vcall.billing.repository;

import com.vcall.billing.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    List<UsageRecord> findBySubscriberIdAndUsageTypeAndRecordedAtBetween(
            UUID subscriberId, UsageRecord.UsageType usageType, LocalDateTime start, LocalDateTime end);

    Optional<UsageRecord> findBySourceAndSourceId(UsageRecord.UsageSource source, String sourceId);
}
