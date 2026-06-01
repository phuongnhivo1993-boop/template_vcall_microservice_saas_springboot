package com.vcall.notification.repository;

import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByName(String name);

    List<NotificationTemplate> findByChannelAndIsActiveTrue(NotificationChannel channel);
}
