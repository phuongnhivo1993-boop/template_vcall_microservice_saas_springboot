package com.vcall.crm.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.crm.dto.OpportunityRequest;
import com.vcall.crm.dto.OpportunityResponse;
import com.vcall.crm.entity.Lead;
import com.vcall.crm.entity.Opportunity;
import com.vcall.crm.entity.OpportunityStage;
import com.vcall.crm.kafka.CrmEventPublisher;
import com.vcall.crm.repository.LeadRepository;
import com.vcall.crm.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final LeadRepository leadRepository;
    private final CrmEventPublisher eventPublisher;

    @Transactional
    public OpportunityResponse createOpportunity(OpportunityRequest request) {
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + request.getLeadId()));

        Opportunity opportunity = new Opportunity();
        mapToEntity(request, opportunity);
        opportunity.setLead(lead);
        opportunity = opportunityRepository.save(opportunity);
        return mapToResponse(opportunity);
    }

    public OpportunityResponse getOpportunity(UUID id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));
        return mapToResponse(opportunity);
    }

    public Page<OpportunityResponse> getAllOpportunities(Pageable pageable) {
        return opportunityRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<OpportunityResponse> getOpportunitiesByLeadId(UUID leadId, Pageable pageable) {
        return opportunityRepository.findByLeadId(leadId, pageable).map(this::mapToResponse);
    }

    public Page<OpportunityResponse> getOpportunitiesByStage(OpportunityStage stage, Pageable pageable) {
        return opportunityRepository.findByStage(stage, pageable).map(this::mapToResponse);
    }

    @Transactional
    public OpportunityResponse updateOpportunity(UUID id, OpportunityRequest request) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        if (request.getLeadId() != null) {
            Lead lead = leadRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + request.getLeadId()));
            opportunity.setLead(lead);
        }
        mapToEntity(request, opportunity);
        opportunity = opportunityRepository.save(opportunity);
        return mapToResponse(opportunity);
    }

    @Transactional
    public OpportunityResponse updateStage(UUID id, OpportunityStage stage) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));
        opportunity.setStage(stage);
        opportunity = opportunityRepository.save(opportunity);
        eventPublisher.publishOpportunityStageChanged(opportunity);
        return mapToResponse(opportunity);
    }

    @Transactional
    public void deleteOpportunity(UUID id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));
        opportunity.setIsDeleted(true);
        opportunityRepository.save(opportunity);
    }

    private void mapToEntity(OpportunityRequest request, Opportunity opportunity) {
        opportunity.setTitle(request.getTitle());
        opportunity.setDescription(request.getDescription());
        opportunity.setValue(request.getValue());
        opportunity.setCurrency(request.getCurrency());
        opportunity.setStage(request.getStage());
        opportunity.setProbability(request.getProbability());
        opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
        opportunity.setAssignedTo(request.getAssignedTo());
    }

    private OpportunityResponse mapToResponse(Opportunity opportunity) {
        return OpportunityResponse.builder()
                .id(opportunity.getId())
                .leadId(opportunity.getLead().getId())
                .leadName(opportunity.getLead().getFirstName() + " " + opportunity.getLead().getLastName())
                .title(opportunity.getTitle())
                .description(opportunity.getDescription())
                .value(opportunity.getValue())
                .currency(opportunity.getCurrency())
                .stage(opportunity.getStage())
                .probability(opportunity.getProbability())
                .expectedCloseDate(opportunity.getExpectedCloseDate())
                .assignedTo(opportunity.getAssignedTo())
                .createdAt(opportunity.getCreatedAt())
                .build();
    }
}
