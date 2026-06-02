package com.vcall.customer.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.customer.dto.CustomerAddressResponse;
import com.vcall.customer.dto.CustomerContactResponse;
import com.vcall.customer.dto.CustomerRequest;
import com.vcall.customer.dto.CustomerResponse;
import com.vcall.customer.dto.CustomerTagResponse;
import com.vcall.customer.entity.Customer;
import com.vcall.customer.entity.CustomerTag;
import com.vcall.customer.entity.CustomerTagMapping;
import com.vcall.customer.repository.CustomerRepository;
import com.vcall.customer.repository.CustomerTagMappingRepository;
import com.vcall.customer.repository.CustomerTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerTagRepository customerTagRepository;
    private final CustomerTagMappingRepository customerTagMappingRepository;

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return toResponse(customer);
    }

    public CustomerResponse create(CustomerRequest request) {
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already exists: " + request.getPhone());
        }

        Customer customer = new Customer();
        customer.setCustomerCode(generateCustomerCode());
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setGender(request.getGender());
        customer.setIdNumber(request.getIdNumber());
        customer.setNationality(request.getNationality());
        customer.setCompany(request.getCompany());
        customer.setPosition(request.getPosition());
        customer.setNotes(request.getNotes());

        Customer saved = customerRepository.save(customer);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<CustomerTag> tags = new HashSet<>(customerTagRepository.findAllById(request.getTagIds()));
            for (CustomerTag tag : tags) {
                CustomerTagMapping mapping = new CustomerTagMapping();
                mapping.setCustomer(saved);
                mapping.setTag(tag);
                customerTagMappingRepository.save(mapping);
            }
        }

        return toResponse(saved);
    }

    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().equals(customer.getPhone())
                && customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already exists: " + request.getPhone());
        }

        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setGender(request.getGender());
        customer.setIdNumber(request.getIdNumber());
        customer.setNationality(request.getNationality());
        customer.setCompany(request.getCompany());
        customer.setPosition(request.getPosition());
        customer.setNotes(request.getNotes());

        Customer saved = customerRepository.save(customer);

        if (request.getTagIds() != null) {
            customerTagMappingRepository.deleteAll(customerTagMappingRepository.findByCustomerId(id));
            Set<CustomerTag> tags = new HashSet<>(customerTagRepository.findAllById(request.getTagIds()));
            for (CustomerTag tag : tags) {
                CustomerTagMapping mapping = new CustomerTagMapping();
                mapping.setCustomer(saved);
                mapping.setTag(tag);
                customerTagMappingRepository.save(mapping);
            }
        }

        return toResponse(saved);
    }

    public void delete(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customer.setIsDeleted(true);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(String keyword, Pageable pageable) {
        return customerRepository.findAll((root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(root.get("phone"), pattern)
            );
        }, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findByTag(Long tagId) {
        List<CustomerTagMapping> mappings = customerTagMappingRepository.findAll();
        return mappings.stream()
                .filter(m -> m.getTag().getId().equals(tagId))
                .map(m -> toResponse(m.getCustomer()))
                .collect(Collectors.toList());
    }

    private String generateCustomerCode() {
        return "CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private CustomerResponse toResponse(Customer customer) {
        Set<CustomerTagResponse> tags = customerTagMappingRepository.findByCustomerId(customer.getId())
                .stream()
                .map(m -> CustomerTagResponse.builder()
                        .id(m.getTag().getId())
                        .name(m.getTag().getName())
                        .color(m.getTag().getColor())
                        .build())
                .collect(Collectors.toSet());

        List<CustomerContactResponse> contacts = customer.getContacts().stream()
                .map(c -> CustomerContactResponse.builder()
                        .id(c.getId())
                        .contactType(c.getContactType().name())
                        .contactValue(c.getContactValue())
                        .isPrimary(c.isPrimary())
                        .build())
                .collect(Collectors.toList());

        List<CustomerAddressResponse> addresses = customer.getAddresses().stream()
                .map(a -> CustomerAddressResponse.builder()
                        .id(a.getId())
                        .addressType(a.getAddressType().name())
                        .addressLine1(a.getAddressLine1())
                        .addressLine2(a.getAddressLine2())
                        .city(a.getCity())
                        .state(a.getState())
                        .country(a.getCountry())
                        .zipCode(a.getZipCode())
                        .isPrimary(a.isPrimary())
                        .build())
                .collect(Collectors.toList());

        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .idNumber(customer.getIdNumber())
                .nationality(customer.getNationality())
                .company(customer.getCompany())
                .position(customer.getPosition())
                .notes(customer.getNotes())
                .tags(tags)
                .contacts(contacts)
                .addresses(addresses)
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
