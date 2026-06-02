package com.vcall.billing.service;

import com.vcall.billing.dto.SubscriptionRequest;
import com.vcall.billing.dto.SubscriptionResponse;
import com.vcall.billing.dto.SubscriptionStatusRequest;
import com.vcall.billing.entity.PricingPlan;
import com.vcall.billing.entity.Subscription;
import com.vcall.billing.kafka.BillingEventPublisher;
import com.vcall.billing.repository.PricingPlanRepository;
import com.vcall.billing.repository.SubscriptionRepository;
import com.vcall.common.exception.BadRequestException;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PricingPlanRepository pricingPlanRepository;
    private final BillingEventPublisher eventPublisher;

    @Transactional
    public SubscriptionResponse updateSubscription(Long id, SubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        if (request.getPlanId() != null) {
            PricingPlan plan = pricingPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pricing plan not found with id: " + request.getPlanId()));
            subscription.setPlan(plan);
        }
        if (request.getSubscriberId() != null) subscription.setSubscriberId(request.getSubscriberId());
        if (request.getStartDate() != null) subscription.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) subscription.setEndDate(request.getEndDate());
        if (request.getAutoRenew() != null) subscription.setAutoRenew(request.getAutoRenew());
        if (request.getTrialEndDate() != null) subscription.setTrialEndDate(request.getTrialEndDate());
        subscription = subscriptionRepository.save(subscription);

        eventPublisher.publishSubscriptionUpdated(subscription);
        return toResponse(subscription);
    }

    @Transactional
    public void deleteSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        subscription.setIsDeleted(true);
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELED);
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);

        eventPublisher.publishSubscriptionDeleted(subscription);
    }

    @Transactional
    public SubscriptionResponse updateSubscriptionStatus(Long id, SubscriptionStatusRequest request) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        subscription.setStatus(request.getStatus());
        if (request.getStatus() == Subscription.SubscriptionStatus.CANCELED) {
            subscription.setCanceledAt(LocalDateTime.now());
            subscription.setAutoRenew(false);
        }
        subscription = subscriptionRepository.save(subscription);

        eventPublisher.publishSubscriptionStatusChanged(subscription);
        return toResponse(subscription);
    }

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
    public Page<SubscriptionResponse> getSubscriptionsBySubscriber(UUID subscriberId, Pageable pageable) {
        return subscriptionRepository.findBySubscriberId(subscriberId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getActiveSubscriptions(Pageable pageable) {
        return subscriptionRepository.findByStatus(Subscription.SubscriptionStatus.ACTIVE, pageable)
                .map(this::toResponse);
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
