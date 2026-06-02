package com.vcall.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.ticket.dto.TicketAssignRequest;
import com.vcall.ticket.dto.TicketRequest;
import com.vcall.ticket.dto.TicketResponse;
import com.vcall.ticket.dto.TicketStatusRequest;
import com.vcall.ticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void createTicket_ShouldReturnCreated() throws Exception {
        TicketRequest request = new TicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setCustomerId(UUID.randomUUID());
        request.setSource("PORTAL");
        request.setCategory("SUPPORT");
        request.setPriority("HIGH");

        TicketResponse response = TicketResponse.builder()
                .id(UUID.randomUUID())
                .ticketNumber("TKT24010100001")
                .title("Test Ticket")
                .description("Test Description")
                .source("PORTAL")
                .category("SUPPORT")
                .priority("HIGH")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        when(ticketService.createTicket(any(TicketRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Ticket created successfully"))
                .andExpect(jsonPath("$.data.title").value("Test Ticket"));
    }

    @Test
    void getTicketById_ShouldReturnTicket() throws Exception {
        UUID id = UUID.randomUUID();
        TicketResponse response = TicketResponse.builder()
                .id(id)
                .ticketNumber("TKT24010100001")
                .title("Test Ticket")
                .status("OPEN")
                .priority("HIGH")
                .build();

        when(ticketService.getTicket(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Ticket"));
    }

    @Test
    void getByTicketNumber_ShouldReturnTicket() throws Exception {
        String ticketNumber = "TKT24010100001";
        TicketResponse response = TicketResponse.builder()
                .id(UUID.randomUUID())
                .ticketNumber(ticketNumber)
                .title("By Number")
                .build();

        when(ticketService.getByTicketNumber(ticketNumber)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tickets/number/{ticketNumber}", ticketNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ticketNumber").value(ticketNumber));
    }

    @Test
    void getAllTickets_ShouldReturnPagedResponse() throws Exception {
        TicketResponse ticket = TicketResponse.builder()
                .id(UUID.randomUUID())
                .title("Test")
                .build();
        Page<TicketResponse> page = new PageImpl<>(List.of(ticket));
        when(ticketService.getAllTickets(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tickets")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Test"));
    }

    @Test
    void updateTicket_ShouldReturnUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        TicketRequest request = new TicketRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Desc");
        request.setCustomerId(UUID.randomUUID());
        request.setSource("CHAT");
        request.setCategory("BILLING");
        request.setPriority("MEDIUM");

        TicketResponse response = TicketResponse.builder()
                .id(id)
                .title("Updated Title")
                .priority("MEDIUM")
                .build();

        when(ticketService.updateTicket(any(UUID.class), any(TicketRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ticket updated successfully"));
    }

    @Test
    void deleteTicket_ShouldReturnSuccess() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ticket deleted successfully"));
    }

    @Test
    void updateStatus_ShouldReturnUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        TicketStatusRequest statusRequest = new TicketStatusRequest("IN_PROGRESS", "Working on it");

        TicketResponse response = TicketResponse.builder()
                .id(id)
                .status("IN_PROGRESS")
                .build();

        when(ticketService.updateStatus(any(UUID.class), any(TicketStatusRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    void assignTicket_ShouldReturnAssigned() throws Exception {
        UUID id = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        TicketAssignRequest assignRequest = new TicketAssignRequest(assigneeId);

        TicketResponse response = TicketResponse.builder()
                .id(id)
                .assignedTo(assigneeId)
                .build();

        when(ticketService.assignTicket(any(UUID.class), any(TicketAssignRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/tickets/{id}/assign", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ticket assigned successfully"));
    }

    @Test
    void searchTickets_ShouldReturnResults() throws Exception {
        TicketResponse ticket = TicketResponse.builder()
                .id(UUID.randomUUID())
                .title("Search Result")
                .build();
        Page<TicketResponse> page = new PageImpl<>(List.of(ticket));
        when(ticketService.search(any(), any(), any(), any(), any(), any(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tickets/search")
                        .param("q", "test")
                        .param("status", "OPEN")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Search Result"));
    }

    @Test
    void createTicket_ValidationFailure() throws Exception {
        TicketRequest request = new TicketRequest();

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
