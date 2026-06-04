package com.vcall.customer.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.customer.dto.CustomerRequest;
import com.vcall.customer.dto.CustomerResponse;
import com.vcall.customer.entity.Customer;
import com.vcall.customer.entity.CustomerTag;
import com.vcall.customer.entity.CustomerTagMapping;
import com.vcall.customer.mapper.CustomerMapper;
import com.vcall.customer.repository.CustomerRepository;
import com.vcall.customer.repository.CustomerTagMappingRepository;
import com.vcall.customer.repository.CustomerTagRepository;
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

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerTagRepository customerTagRepository;

    @Mock
    private CustomerTagMappingRepository customerTagMappingRepository;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private CustomerService customerService;
    private Customer customer;
    private CustomerRequest request;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, customerTagRepository, customerTagMappingRepository, mock(CustomerMapper.class));

        customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setCustomerCode("CUS-TEST");
        customer.setFullName("John Doe");
        customer.setEmail("john@example.com");
        customer.setPhone("1234567890");
        customer.setGender("male");
        customer.setCompany("Acme Corp");
        customer.setNationality("US");
        customer.setIsDeleted(false);

        request = new CustomerRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setPhone("1234567890");
        request.setGender("male");
        request.setCompany("Acme Corp");
        request.setNationality("US");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testFindAll() {
        Page<Customer> page = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<CustomerResponse> result = customerService.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe");
        verify(customerRepository).findAll(any(PageRequest.class));
    }

    @Test
    void testFindAllWithFilters() {
        Page<Customer> page = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<CustomerResponse> result = customerService.findAll(
                "John", "male", "Acme", "US",
                null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(customerRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void testFindById_Success() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        CustomerResponse result = customerService.findById(customer.getId());

        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void testCreate_Success() {
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(customerRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse result = customerService.create(request);

        assertThat(result.getFullName()).isEqualTo("John Doe");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void testCreate_DuplicateEmail() {
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void testCreate_DuplicatePhone() {
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(customerRepository.existsByPhone(request.getPhone())).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Phone already exists");
    }

    @Test
    void testUpdate_Success() {
        UUID id = customer.getId();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        request.setFullName("Jane Doe");
        CustomerResponse result = customerService.update(id, request);

        assertThat(result.getFullName()).isEqualTo("John Doe");
        verify(customerRepository).save(customerCaptor.capture());
        assertThat(customerCaptor.getValue().getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void testUpdate_NotFound() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.update(id, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testUpdate_DuplicateEmail() {
        UUID id = customer.getId();
        Customer otherCustomer = new Customer();
        otherCustomer.setId(UUID.randomUUID());
        otherCustomer.setEmail("other@example.com");

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("other@example.com")).thenReturn(true);

        request.setEmail("other@example.com");
        assertThatThrownBy(() -> customerService.update(id, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void testDelete_Success() {
        UUID id = customer.getId();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerService.delete(id);

        verify(customerRepository).save(customerCaptor.capture());
        assertThat(customerCaptor.getValue().getIsDeleted()).isTrue();
    }

    @Test
    void testDelete_NotFound() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testSearch() {
        Page<Customer> page = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<CustomerResponse> result = customerService.search("John", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(customerRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void testCreateWithTags() {
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(customerRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Set<Long> tagIds = Set.of(1L, 2L);
        request.setTagIds(tagIds);

        CustomerTag tag1 = new CustomerTag();
        tag1.setId(1L);
        tag1.setName("VIP");
        CustomerTag tag2 = new CustomerTag();
        tag2.setId(2L);
        tag2.setName("New");

        when(customerTagRepository.findAllById(tagIds)).thenReturn(List.of(tag1, tag2));

        customerService.create(request);

        verify(customerTagMappingRepository, times(2)).save(any(CustomerTagMapping.class));
    }

    @Test
    void testFindByTag() {
        CustomerTag tag = new CustomerTag();
        tag.setId(1L);
        tag.setName("VIP");

        CustomerTagMapping mapping = new CustomerTagMapping();
        mapping.setCustomer(customer);
        mapping.setTag(tag);

        when(customerTagMappingRepository.findAll()).thenReturn(List.of(mapping));

        List<CustomerResponse> result = customerService.findByTag(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
    }
}
