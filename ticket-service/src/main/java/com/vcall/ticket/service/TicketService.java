package com.vcall.ticket.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.ticket.dto.TicketAssignRequest;
import com.vcall.ticket.dto.TicketRequest;
import com.vcall.ticket.dto.TicketResponse;
import com.vcall.ticket.dto.TicketStatusRequest;
import com.vcall.ticket.entity.Ticket;
import com.vcall.ticket.entity.Ticket.TicketPriority;
import com.vcall.ticket.entity.Ticket.TicketSource;
import com.vcall.ticket.entity.Ticket.TicketStatus;
import com.vcall.ticket.entity.TicketStatusHistory;
import com.vcall.ticket.kafka.TicketEventPublisher;
import com.vcall.ticket.repository.TicketRepository;
import com.vcall.ticket.repository.TicketStatusHistoryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository statusHistoryRepository;
    private final TicketEventPublisher eventPublisher;
    private final SlaService slaService;

    private static final AtomicLong ticketCounter = new AtomicLong(0);

    @CircuitBreaker(name = "ticketService", fallbackMethod = "createTicketFallback")
    @Retry(name = "ticketService")
    @Transactional
    public TicketResponse createTicket(TicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCustomerId(request.getCustomerId());
        ticket.setSource(TicketSource.valueOf(request.getSource().toUpperCase()));
        ticket.setCategory(request.getCategory());
        ticket.setPriority(TicketPriority.valueOf(request.getPriority().toUpperCase()));
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAssignedTo(request.getAssignedTo());
        ticket.setRelatedCallId(request.getRelatedCallId());
        ticket.setRelatedConversationId(request.getRelatedConversationId());
        ticket = ticketRepository.save(ticket);

        recordStatusHistory(ticket, null, TicketStatus.OPEN, "system", "Ticket created");

        eventPublisher.publishTicketCreated(ticket);

        slaService.checkSlaBreach(ticket);

        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getByTicketNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStatsByStatus() {
        return ticketRepository.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));
    }

    @Transactional
    public TicketResponse updateTicket(UUID id, TicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(TicketPriority.valueOf(request.getPriority().toUpperCase()));
        ticket.setSource(TicketSource.valueOf(request.getSource().toUpperCase()));
        ticket.setAssignedTo(request.getAssignedTo());
        ticket.setRelatedCallId(request.getRelatedCallId());
        ticket.setRelatedConversationId(request.getRelatedConversationId());
        ticket = ticketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Transactional
    public void deleteTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        ticket.setIsDeleted(true);
        ticketRepository.save(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(UUID id, TicketStatusRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        TicketStatus newStatus = TicketStatus.valueOf(request.getStatus().toUpperCase());
        TicketStatus oldStatus = ticket.getStatus();

        if (newStatus == oldStatus) {
            return toResponse(ticket);
        }

        ticket.setStatus(newStatus);

        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        ticket = ticketRepository.save(ticket);

        recordStatusHistory(ticket, oldStatus, newStatus, "system", request.getReason());

        if (newStatus == TicketStatus.CLOSED) {
            eventPublisher.publishTicketClosed(ticket);
        }

        slaService.checkSlaBreach(ticket);

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(UUID id, TicketAssignRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        ticket.setAssignedTo(request.getAssignedTo());
        ticket = ticketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getByAssignee(UUID assignedTo) {
        return ticketRepository.findByAssignedTo(assignedTo).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> search(String q, String status, String priority, UUID assignedTo,
                                       LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isEmpty()) {
                String pattern = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("ticketNumber")), pattern)
                ));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), TicketStatus.valueOf(status.toUpperCase())));
            }
            if (priority != null && !priority.isEmpty()) {
                predicates.add(cb.equal(root.get("priority"), TicketPriority.valueOf(priority.toUpperCase())));
            }
            if (assignedTo != null) {
                predicates.add(cb.equal(root.get("assignedTo"), assignedTo));
            }
            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("createdAt"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            } else if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ticketRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public TicketResponse escalateTicket(UUID id, String reason) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        ticket.setPriority(TicketPriority.URGENT);
        ticket = ticketRepository.save(ticket);

        recordStatusHistory(ticket, ticket.getStatus(), ticket.getStatus(), "system", "Escalated: " + reason);

        eventPublisher.publishTicketEscalated(ticket, reason);

        slaService.checkSlaBreach(ticket);

        return toResponse(ticket);
    }

    private String generateTicketNumber() {
        String prefix = "TKT";
        long seq = ticketCounter.incrementAndGet() % 100000;
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        return prefix + datePart + String.format("%05d", seq);
    }

    private void recordStatusHistory(Ticket ticket, TicketStatus from, TicketStatus to, String changedBy, String reason) {
        TicketStatusHistory history = new TicketStatusHistory();
        history.setTicket(ticket);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);
    }

    private TicketResponse toResponse(Ticket ticket) {
        String slaStatus = slaService.getSlaStatus(ticket);
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .customerId(ticket.getCustomerId())
                .source(ticket.getSource().name())
                .category(ticket.getCategory())
                .priority(ticket.getPriority().name())
                .status(ticket.getStatus().name())
                .assignedTo(ticket.getAssignedTo())
                .firstResponseAt(ticket.getFirstResponseAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .slaStatus(slaStatus)
                .createdAt(ticket.getCreatedAt())
                .build();
    }

    private TicketResponse createTicketFallback(TicketRequest request, Exception ex) {
        log.error("Circuit breaker triggered for createTicket: {}", ex.getMessage());
        throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    }
}
