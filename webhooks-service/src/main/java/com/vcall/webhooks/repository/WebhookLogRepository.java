package com.vcall.webhooks.repository;

import com.vcall.webhooks.entity.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
    List<WebhookLog> findByWebhookId(Long webhookId);
}
