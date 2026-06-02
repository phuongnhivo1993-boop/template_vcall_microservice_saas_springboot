package com.vcall.customer360.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.customer360.dto.Customer360Response;
import com.vcall.customer360.entity.CustomerProfile;
import com.vcall.customer360.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class Customer360Service {

    private final CustomerProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public Customer360Response getCustomer360(UUID customerId) {
        CustomerProfile profile = profileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found: " + customerId));
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public Page<Customer360Response> search(String keyword, String segment, String sortBy, String sortDir, int page, int size) {
        Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return profileRepository.findAll((root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(root.get("phone"), pattern)
                ));
            }
            if (segment != null && !segment.isEmpty()) {
                predicates.add(cb.equal(root.get("segment"), segment));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, PageRequest.of(page, size, sort)).map(this::toResponse);
    }

    public Customer360Response updateProfile(UUID customerId, Map<String, Object> updates) {
        CustomerProfile profile = profileRepository.findByCustomerId(customerId)
                .orElseGet(() -> CustomerProfile.builder().customerId(customerId).build());

        if (updates.containsKey("fullName")) profile.setFullName((String) updates.get("fullName"));
        if (updates.containsKey("email")) profile.setEmail((String) updates.get("email"));
        if (updates.containsKey("phone")) profile.setPhone((String) updates.get("phone"));
        if (updates.containsKey("segment")) profile.setSegment((String) updates.get("segment"));
        if (updates.containsKey("notes")) profile.setNotes((String) updates.get("notes"));

        profileRepository.save(profile);
        log.info("Customer 360 profile updated for customerId: {}", customerId);
        return toResponse(profile);
    }

    private Customer360Response toResponse(CustomerProfile profile) {
        return Customer360Response.builder()
                .id(profile.getId())
                .customerId(profile.getCustomerId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .totalCalls(profile.getTotalCalls())
                .totalTickets(profile.getTotalTickets())
                .totalLeads(profile.getTotalLeads())
                .totalOpportunities(profile.getTotalOpportunities())
                .totalSpent(profile.getTotalSpent())
                .lastContactAt(profile.getLastContactAt())
                .lifetimeValue(profile.getLifetimeValue())
                .satisfactionScore(profile.getSatisfactionScore())
                .segment(profile.getSegment())
                .notes(profile.getNotes())
                .build();
    }
}
