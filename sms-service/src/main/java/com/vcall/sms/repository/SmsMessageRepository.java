package com.vcall.sms.repository;

import com.vcall.sms.entity.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SmsMessageRepository extends JpaRepository<SmsMessage, UUID>, JpaSpecificationExecutor<SmsMessage> {

    Optional<SmsMessage> findByMessageId(String messageId);

    List<SmsMessage> findByFromNumber(String fromNumber);

    List<SmsMessage> findByToNumber(String toNumber);

    List<SmsMessage> findByDirectionAndStatus(SmsMessage.SmsDirection direction, SmsMessage.SmsStatus status);

    List<SmsMessage> findBySentAtBetween(LocalDateTime start, LocalDateTime end);

    List<SmsMessage> findByStatus(SmsMessage.SmsStatus status);
}
