package com.vcall.webhooks.repository;

import com.vcall.webhooks.entity.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
}
