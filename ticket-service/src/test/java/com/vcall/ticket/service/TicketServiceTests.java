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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTests {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStatusHistoryRepository statusHistoryRepository;

    @Mock
    private TicketEventPublisher eventPublisher;

    @Mock
    private SlaService slaService;

    @Captor
    private ArgumentCaptor<Ticket> ticketCaptor;

    private TicketService ticketService;
    private Ticket ticket;
    private TicketRequest request;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, statusHistoryRepository, eventPublisher, slaService);

        ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTicketNumber("TKT24010100001");
        ticket.setTitle("Test Ticket");
        ticket.setDescription("Test Description");
        ticket.setCustomerId(UUID.randomUUID());
        ticket.setSource(TicketSource.PORTAL);
        ticket.setCategory("SUPPORT");
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setIsDeleted(false);

        request = new TicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setCustomerId(UUID.randomUUID());
        request.setSource("PORTAL");
        request.setCategory("SUPPORT");
        request.setPriority("HIGH");
    }

    @Test
    void createTicket_Success() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(slaService.getSlaStatus(any(Ticket.class))).thenReturn("PENDING");

        TicketResponse result = ticketService.createTicket(request);

        assertThat(result.getTitle()).isEqualTo("Test Ticket");
        assertThat(result.getStatus()).isEqualTo("OPEN");
        verify(ticketRepository).save(any(Ticket.class));
        verify(eventPublisher).publishTicketCreated(any(Ticket.class));
        verify(statusHistoryRepository).save(any(TicketStatusHistory.class));
    }

    @Test
    void getTicket_Success() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        TicketResponse result = ticketService.getTicket(ticket.getId());

        assertThat(result.getTitle()).isEqualTo("Test Ticket");
        assertThat(result.getTicketNumber()).isEqualTo("TKT24010100001");
    }

    @Test
    void getTicket_NotFound() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicket(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found with id");
    }

    @Test
    void getByTicketNumber_Success() {
        when(ticketRepository.findByTicketNumber("TKT24010100001")).thenReturn(Optional.of(ticket));
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        TicketResponse result = ticketService.getByTicketNumber("TKT24010100001");

        assertThat(result.getTicketNumber()).isEqualTo("TKT24010100001");
    }

    @Test
    void getByTicketNumber_NotFound() {
        when(ticketRepository.findByTicketNumber("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getByTicketNumber("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllTickets() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(ticketRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        Page<TicketResponse> result = ticketService.getAllTickets(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Ticket");
    }

    @Test
    void updateTicket_Success() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        request.setTitle("Updated Title");
        TicketResponse result = ticketService.updateTicket(ticket.getId(), request);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(ticketRepository).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void updateTicket_NotFound() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.updateTicket(id, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTicket_Success() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.deleteTicket(ticket.getId());

        verify(ticketRepository).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getIsDeleted()).isTrue();
    }

    @Test
    void deleteTicket_NotFound() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.deleteTicket(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_Success() {
        TicketStatusRequest statusRequest = new TicketStatusRequest("IN_PROGRESS", "Working on it");
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        TicketResponse result = ticketService.updateStatus(ticket.getId(), statusRequest);

        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
        verify(ticketRepository).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_ToClosed_PublishesEvent() {
        TicketStatusRequest statusRequest = new TicketStatusRequest("CLOSED", "Resolved");
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(slaService.getSlaStatus(ticket)).thenReturn("MET");

        ticketService.updateStatus(ticket.getId(), statusRequest);

        verify(eventPublisher).publishTicketClosed(any(Ticket.class));
    }

    @Test
    void assignTicket_Success() {
        UUID assigneeId = UUID.randomUUID();
        TicketAssignRequest assignRequest = new TicketAssignRequest(assigneeId);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        TicketResponse result = ticketService.assignTicket(ticket.getId(), assignRequest);

        verify(ticketRepository).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getAssignedTo()).isEqualTo(assigneeId);
    }

    @Test
    void getByStatus() {
        when(ticketRepository.findByStatus(TicketStatus.OPEN)).thenReturn(List.of(ticket));
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        List<TicketResponse> results = ticketService.getByStatus(TicketStatus.OPEN);

        assertThat(results).hasSize(1);
    }

    @Test
    void getByAssignee() {
        UUID assigneeId = UUID.randomUUID();
        ticket.setAssignedTo(assigneeId);
        when(ticketRepository.findByAssignedTo(assigneeId)).thenReturn(List.of(ticket));
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        List<TicketResponse> results = ticketService.getByAssignee(assigneeId);

        assertThat(results).hasSize(1);
    }

    @Test
    void search() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(ticketRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        Page<TicketResponse> result = ticketService.search("test", "OPEN", "HIGH", null, null, null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void escalateTicket_SetsUrgentAndPublishes() {
        String reason = "Customer escalation";
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(slaService.getSlaStatus(ticket)).thenReturn("PENDING");

        ticketService.escalateTicket(ticket.getId(), reason);

        verify(ticketRepository).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getPriority()).isEqualTo(TicketPriority.URGENT);
        verify(eventPublisher).publishTicketEscalated(any(Ticket.class), eq(reason));
    }

    @Test
    void getStatsByStatus() {
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));

        var stats = ticketService.getStatsByStatus();

        assertThat(stats).containsKey("OPEN");
        assertThat(stats.get("OPEN")).isEqualTo(1);
    }
}
