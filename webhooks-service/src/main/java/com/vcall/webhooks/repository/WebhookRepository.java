package com.vcall.webhooks.repository;

import com.vcall.webhooks.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WebhookRepository extends JpaRepository<Webhook, Long>, JpaSpecificationExecutor<Webhook> {

    List<Webhook> findByIsActiveTrue();

    List<Webhook> findByEventsContaining(String event);
}
