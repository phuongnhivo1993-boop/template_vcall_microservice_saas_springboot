package com.vcall.billing.service;

import com.vcall.billing.dto.SubscriptionRequest;
import com.vcall.billing.dto.SubscriptionResponse;
import com.vcall.billing.entity.PricingPlan;
import com.vcall.billing.entity.Subscription;
import com.vcall.billing.kafka.BillingEventPublisher;
import com.vcall.billing.repository.PricingPlanRepository;
import com.vcall.billing.repository.SubscriptionRepository;
import com.vcall.common.exception.BadRequestException;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PricingPlanRepository pricingPlanRepository;
    private final BillingEventPublisher eventPublisher;

    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        PricingPlan plan = pricingPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Pricing plan not found with id: " + request.getPlanId()));

        Subscription subscription = new Subscription();
        subscription.setSubscriberId(request.getSubscriberId());
        subscription.setPlan(plan);
        subscription.setStartDate(request.getStartDate());
        subscription.setEndDate(request.getEndDate());
        subscription.setAutoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : true);
        subscription.setTrialEndDate(request.getTrialEndDate());
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        if (subscription.getStatus() == Subscription.SubscriptionStatus.CANCELED) {
            throw new BadRequestException("Subscription is already canceled");
        }
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELED);
        subscription.setAutoRenew(false);
        subscription.setCanceledAt(LocalDateTime.now());
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse renewSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        if (subscription.getStatus() == Subscription.SubscriptionStatus.CANCELED) {
            throw new BadRequestException("Cannot renew a canceled subscription");
        }
        PricingPlan plan = subscription.getPlan();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newEndDate;
        switch (plan.getBillingCycle()) {
            case MONTHLY -> newEndDate = now.plusMonths(1);
            case QUARTERLY -> newEndDate = now.plusMonths(3);
            case ANNUAL -> newEndDate = now.plusYears(1);
            default -> newEndDate = now.plusMonths(1);
        }
        subscription.setStartDate(now);
        subscription.setEndDate(newEndDate);
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setCanceledAt(null);
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        return toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsBySubscriber(UUID subscriberId) {
        return subscriptionRepository.findBySubscriberId(subscriberId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getActiveSubscriptions() {
        return subscriptionRepository.findByStatus(Subscription.SubscriptionStatus.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkExpiration() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findByEndDateBefore(now);
        for (Subscription sub : expiredSubscriptions) {
            if (sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE) {
                sub.setStatus(Subscription.SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
                log.info("Subscription {} expired for subscriber {}", sub.getId(), sub.getSubscriberId());
            }
        }

        LocalDateTime expiringSoon = now.plusDays(7);
        List<Subscription> expiringSubscriptions = subscriptionRepository
                .findByStatusAndEndDateBetween(Subscription.SubscriptionStatus.ACTIVE, now, expiringSoon);
        for (Subscription sub : expiringSubscriptions) {
            eventPublisher.publishSubscriptionExpiring(sub);
        }
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .planName(subscription.getPlan().getName())
                .subscriberId(subscription.getSubscriberId())
                .status(subscription.getStatus().name())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .autoRenew(subscription.getAutoRenew())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
