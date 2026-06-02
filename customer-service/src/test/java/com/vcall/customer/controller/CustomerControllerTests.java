package com.vcall.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.customer.dto.CustomerRequest;
import com.vcall.customer.dto.CustomerResponse;
import com.vcall.customer.kafka.CustomerEventPublisher;
import com.vcall.customer.service.CustomerAddressService;
import com.vcall.customer.service.CustomerContactService;
import com.vcall.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomerContactService customerContactService;

    @MockitoBean
    private CustomerAddressService customerAddressService;

    @MockitoBean
    private CustomerEventPublisher customerEventPublisher;

    @Test
    void testGetAll_ReturnsPagedResponse() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .email("john@example.com")
                .build();
        Page<CustomerResponse> page = new PageImpl<>(List.of(response));
        when(customerService.findAll(any(), any(), any(), any(), any(), any(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].fullName").value("John Doe"));
    }

    @Test
    void testGetById_ReturnsCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerResponse response = CustomerResponse.builder()
                .id(id)
                .fullName("John Doe")
                .email("john@example.com")
                .build();
        when(customerService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }

    @Test
    void testCreate_ReturnsCreated() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");

        CustomerResponse response = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .email("john@example.com")
                .build();
        when(customerService.create(any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer created"));
    }

    @Test
    void testCreate_ValidationFailure() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate_ReturnsUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerRequest request = new CustomerRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");

        CustomerResponse response = CustomerResponse.builder()
                .id(id)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .build();
        when(customerService.update(any(UUID.class), any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer updated"));
    }

    @Test
    void testDelete_ReturnsSuccess() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer deleted"));
    }

    @Test
    void testSearch_ReturnsResults() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .build();
        Page<CustomerResponse> page = new PageImpl<>(List.of(response));
        when(customerService.search(any(String.class), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers/search")
                        .param("keyword", "John")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fullName").value("John Doe"));
    }

    @Test
    void testGetAll_FilteredByGender() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .fullName("Jane Doe")
                .gender("female")
                .build();
        Page<CustomerResponse> page = new PageImpl<>(List.of(response));
        when(customerService.findAll(any(), eq("female"), any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/customers")
                        .param("gender", "female"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].gender").value("female"));
    }

    @Test
    void testCreate_InvalidEmail() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Doe");
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
