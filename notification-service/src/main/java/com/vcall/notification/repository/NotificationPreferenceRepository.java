package com.vcall.notification.repository;

import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationPreference;
import com.vcall.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByUserIdAndChannel(UUID userId, NotificationChannel channel);

    List<NotificationPreference> findByUserId(UUID userId);

    Optional<NotificationPreference> findByUserIdAndChannelAndType(UUID userId, NotificationChannel channel, NotificationType type);
}
