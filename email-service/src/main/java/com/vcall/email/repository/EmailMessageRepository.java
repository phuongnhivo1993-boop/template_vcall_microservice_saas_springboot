package com.vcall.email.repository;

import com.vcall.email.entity.EmailMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID>, JpaSpecificationExecutor<EmailMessage> {

    Optional<EmailMessage> findByMessageId(String messageId);

    List<EmailMessage> findByConversationIdOrderByReceivedAt(UUID conversationId);

    List<EmailMessage> findByFromAddress(String fromAddress);

    List<EmailMessage> findByToAddressesContaining(String emailAddress);

    List<EmailMessage> findByDirectionAndStatus(EmailMessage.EmailDirection direction, EmailMessage.EmailStatus status);

    List<EmailMessage> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);
}
