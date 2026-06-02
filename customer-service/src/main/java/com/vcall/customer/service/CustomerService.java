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
import com.vcall.customer.mapper.CustomerMapper;
import com.vcall.customer.repository.CustomerRepository;
import com.vcall.customer.repository.CustomerTagMappingRepository;
import com.vcall.customer.repository.CustomerTagRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(String keyword, String gender, String company,
                                           String nationality, LocalDate dateFrom, LocalDate dateTo,
                                           Pageable pageable) {
        return customerRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(root.get("phone"), pattern),
                        cb.like(cb.lower(root.get("company")), pattern),
                        cb.like(root.get("customerCode"), pattern)
                ));
            }
            if (gender != null && !gender.isEmpty()) {
                predicates.add(cb.equal(root.get("gender"), gender));
            }
            if (company != null && !company.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("company")), "%" + company.toLowerCase() + "%"));
            }
            if (nationality != null && !nationality.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nationality")), "%" + nationality.toLowerCase() + "%"));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo.atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(this::toResponse);
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

        Customer customer = customerMapper.toEntity(request);
        customer.setCustomerCode(generateCustomerCode());

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

        customerMapper.updateEntity(request, customer);

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
        CustomerResponse response = customerMapper.toResponse(customer);

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

        response.setTags(tags);
        response.setContacts(contacts);
        response.setAddresses(addresses);
        return response;
    }
}
