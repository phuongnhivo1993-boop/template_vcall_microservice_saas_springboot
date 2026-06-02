package com.vcall.crm.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.crm.dto.LeadRequest;
import com.vcall.crm.dto.LeadResponse;
import com.vcall.crm.dto.OpportunityRequest;
import com.vcall.crm.dto.OpportunityResponse;
import com.vcall.crm.entity.Lead;
import com.vcall.crm.entity.LeadStatus;
import com.vcall.crm.entity.Opportunity;
import com.vcall.crm.kafka.CrmEventPublisher;
import com.vcall.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final OpportunityService opportunityService;
    private final CrmEventPublisher eventPublisher;

    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        Lead lead = new Lead();
        mapToEntity(request, lead);
        lead.setStatus(LeadStatus.NEW);
        lead = leadRepository.save(lead);
        eventPublisher.publishLeadCreated(lead);
        return mapToResponse(lead);
    }

    public LeadResponse getLead(UUID id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        return mapToResponse(lead);
    }

    public Page<LeadResponse> searchLeads(Specification<Lead> spec, Pageable pageable) {
        return leadRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public List<LeadResponse> getLeadsByStatus(LeadStatus status) {
        return leadRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public LeadResponse updateLead(UUID id, LeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        mapToEntity(request, lead);
        lead = leadRepository.save(lead);
        return mapToResponse(lead);
    }

    @Transactional
    public LeadResponse updateLeadStatus(UUID id, LeadStatus status) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        lead.setStatus(status);
        lead = leadRepository.save(lead);
        return mapToResponse(lead);
    }

    @Transactional
    public LeadResponse assignLead(UUID id, UUID assignedTo) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        lead.setAssignedTo(assignedTo);
        lead = leadRepository.save(lead);
        return mapToResponse(lead);
    }

    @Transactional
    public OpportunityResponse convertLeadToOpportunity(UUID leadId, OpportunityRequest opportunityRequest) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));
        lead.setStatus(LeadStatus.QUALIFIED);
        leadRepository.save(lead);

        OpportunityResponse opportunity = opportunityService.createOpportunity(opportunityRequest);
        eventPublisher.publishLeadConverted(lead, opportunity);
        return opportunity;
    }

    @Transactional
    public void deleteLead(UUID id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        lead.setIsDeleted(true);
        leadRepository.save(lead);
    }

    public long getLeadCountByStatus(LeadStatus status) {
        return leadRepository.countByStatus(status);
    }

    private void mapToEntity(LeadRequest request, Lead lead) {
        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setCompany(request.getCompany());
        lead.setTitle(request.getTitle());
        lead.setSource(request.getSource());
        lead.setScore(request.getScore());
        lead.setAssignedTo(request.getAssignedTo());
        lead.setNotes(request.getNotes());
    }

    private LeadResponse mapToResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .fullName(lead.getFirstName() + " " + lead.getLastName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .company(lead.getCompany())
                .title(lead.getTitle())
                .source(lead.getSource())
                .status(lead.getStatus())
                .score(lead.getScore())
                .assignedTo(lead.getAssignedTo())
                .createdAt(lead.getCreatedAt())
                .build();
    }
}
