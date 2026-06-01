package com.vcall.notification.repository;

import com.vcall.notification.entity.Notification;
import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationStatus;
import com.vcall.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdOrderBySentAtDesc(UUID recipientId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByChannelAndStatus(NotificationChannel channel, NotificationStatus status);

    List<Notification> findByRecipientIdAndType(UUID recipientId, NotificationType type);

    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
