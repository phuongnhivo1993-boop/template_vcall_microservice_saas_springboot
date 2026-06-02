package com.vcall.ticket.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.ticket.dto.SlaRuleRequest;
import com.vcall.ticket.dto.SlaRuleResponse;
import com.vcall.ticket.entity.SlaBreach;
import com.vcall.ticket.entity.SlaBreach.BreachType;
import com.vcall.ticket.entity.SlaRule;
import com.vcall.ticket.entity.Ticket;
import com.vcall.ticket.entity.Ticket.TicketPriority;
import com.vcall.ticket.entity.Ticket.TicketStatus;
import com.vcall.ticket.kafka.TicketEventPublisher;
import com.vcall.ticket.repository.SlaBreachRepository;
import com.vcall.ticket.repository.SlaRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaService {

    private final SlaRuleRepository slaRuleRepository;
    private final SlaBreachRepository slaBreachRepository;
    private final TicketEventPublisher eventPublisher;

    @Transactional
    public SlaRuleResponse createRule(SlaRuleRequest request) {
        SlaRule rule = new SlaRule();
        rule.setName(request.getName());
        if (request.getPriority() != null) {
            rule.setPriority(TicketPriority.valueOf(request.getPriority().toUpperCase()));
        }
        rule.setCategory(request.getCategory());
        rule.setFirstResponseTime(request.getFirstResponseTime());
        rule.setResolutionTime(request.getResolutionTime());
        rule.setEscalationLevel(request.getEscalationLevel());
        rule.setEscalationNotifyTo(request.getEscalationNotifyTo());
        rule.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        rule = slaRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional(readOnly = true)
    public SlaRuleResponse getRule(Long id) {
        SlaRule rule = slaRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA rule not found with id: " + id));
        return toResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<SlaRuleResponse> getAllRules() {
        return slaRuleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SlaRuleResponse updateRule(Long id, SlaRuleRequest request) {
        SlaRule rule = slaRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA rule not found with id: " + id));
        rule.setName(request.getName());
        if (request.getPriority() != null) {
            rule.setPriority(TicketPriority.valueOf(request.getPriority().toUpperCase()));
        }
        rule.setCategory(request.getCategory());
        rule.setFirstResponseTime(request.getFirstResponseTime());
        rule.setResolutionTime(request.getResolutionTime());
        rule.setEscalationLevel(request.getEscalationLevel());
        rule.setEscalationNotifyTo(request.getEscalationNotifyTo());
        rule.setIsActive(request.getIsActive());
        rule = slaRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        SlaRule rule = slaRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA rule not found with id: " + id));
        rule.setIsDeleted(true);
        slaRuleRepository.save(rule);
    }

    @Transactional(readOnly = true)
    public List<SlaRuleResponse> getActiveRules() {
        return slaRuleRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getSlaStatus(Ticket ticket) {
        SlaRule rule = findMatchingRule(ticket);
        if (rule == null) {
            return "PENDING";
        }

        boolean hasBreach = slaBreachRepository.findByTicketId(ticket.getId()).stream()
                .anyMatch(b -> !b.getNotified());
        if (hasBreach) {
            return "BREACHED";
        }

        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.RESOLVED) {
            return "MET";
        }

        if (ticket.getFirstResponseAt() == null && rule.getFirstResponseTime() != null) {
            long elapsed = ChronoUnit.MINUTES.between(ticket.getCreatedAt(), LocalDateTime.now());
            if (elapsed > rule.getFirstResponseTime()) {
                return "BREACHED";
            }
        }

        if (ticket.getResolvedAt() == null && rule.getResolutionTime() != null) {
            long elapsed = ChronoUnit.MINUTES.between(ticket.getCreatedAt(), LocalDateTime.now());
            if (elapsed > rule.getResolutionTime()) {
                return "BREACHED";
            }
        }

        return "PENDING";
    }

    @Transactional
    public void checkSlaBreach(Ticket ticket) {
        SlaRule rule = findMatchingRule(ticket);
        if (rule == null) {
            return;
        }

        boolean alreadyBreached = slaBreachRepository.findByTicketId(ticket.getId()).stream()
                .anyMatch(b -> !b.getNotified());
        if (alreadyBreached) {
            return;
        }

        if (ticket.getFirstResponseAt() == null && rule.getFirstResponseTime() != null) {
            long elapsed = ChronoUnit.MINUTES.between(ticket.getCreatedAt(), LocalDateTime.now());
            if (elapsed > rule.getFirstResponseTime() && ticket.getStatus() != TicketStatus.CLOSED) {
                createBreach(ticket, rule, BreachType.FIRST_RESPONSE);
            }
        }

        if (ticket.getResolvedAt() == null && rule.getResolutionTime() != null) {
            long elapsed = ChronoUnit.MINUTES.between(ticket.getCreatedAt(), LocalDateTime.now());
            if (elapsed > rule.getResolutionTime() && ticket.getStatus() != TicketStatus.CLOSED) {
                createBreach(ticket, rule, BreachType.RESOLUTION);
            }
        }
    }

    @Transactional
    public void monitorSla() {
        List<SlaRule> activeRules = slaRuleRepository.findByIsActiveTrue();
        List<Ticket> allTickets = List.of();

        for (SlaRule rule : activeRules) {
            List<Ticket> matchingTickets = findTicketsForRule(rule);
            for (Ticket ticket : matchingTickets) {
                checkSlaBreach(ticket);
            }
        }

        sendBreachNotification();
    }

    private void createBreach(Ticket ticket, SlaRule rule, BreachType breachType) {
        SlaBreach breach = new SlaBreach();
        breach.setTicket(ticket);
        breach.setSlaRule(rule);
        breach.setBreachType(breachType);
        breach.setBreachedAt(LocalDateTime.now());
        breach.setNotified(false);
        slaBreachRepository.save(breach);

        log.warn("SLA breach detected for ticket {}: {}", ticket.getTicketNumber(), breachType);
    }

    @Transactional
    public void sendBreachNotification() {
        List<SlaBreach> unnotifiedBreaches = slaBreachRepository.findByNotifiedFalse();
        for (SlaBreach breach : unnotifiedBreaches) {
            eventPublisher.publishSlaBreach(breach);
            breach.setNotified(true);
            slaBreachRepository.save(breach);
        }
    }

    private SlaRule findMatchingRule(Ticket ticket) {
        List<SlaRule> rules = slaRuleRepository.findByIsActiveTrue();
        if (rules.isEmpty()) {
            return null;
        }

        return rules.stream()
                .filter(r -> r.getPriority() == ticket.getPriority()
                        && (r.getCategory() == null || r.getCategory().equalsIgnoreCase(ticket.getCategory())))
                .findFirst()
                .orElseGet(() -> rules.stream()
                        .filter(r -> r.getPriority() == ticket.getPriority() && r.getCategory() == null)
                        .findFirst()
                        .orElse(null));
    }

    private List<Ticket> findTicketsForRule(SlaRule rule) {
        return List.of();
    }

    private SlaRuleResponse toResponse(SlaRule rule) {
        return SlaRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .priority(rule.getPriority() != null ? rule.getPriority().name() : null)
                .category(rule.getCategory())
                .firstResponseTime(rule.getFirstResponseTime())
                .resolutionTime(rule.getResolutionTime())
                .escalationLevel(rule.getEscalationLevel())
                .isActive(rule.getIsActive())
                .build();
    }
}
