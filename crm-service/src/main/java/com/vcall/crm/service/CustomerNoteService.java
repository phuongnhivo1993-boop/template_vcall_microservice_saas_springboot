package com.vcall.crm.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.crm.dto.CustomerNoteRequest;
import com.vcall.crm.dto.CustomerNoteResponse;
import com.vcall.crm.entity.CustomerNote;
import com.vcall.crm.repository.CustomerNoteRepository;
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
public class CustomerNoteService {

    private final CustomerNoteRepository customerNoteRepository;

    @Transactional
    public CustomerNoteResponse createNote(CustomerNoteRequest request) {
        CustomerNote note = new CustomerNote();
        mapToEntity(request, note);
        note = customerNoteRepository.save(note);
        return mapToResponse(note);
    }

    public CustomerNoteResponse getNote(Long id) {
        CustomerNote note = customerNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerNote not found with id: " + id));
        return mapToResponse(note);
    }

    public Page<CustomerNoteResponse> getAllNotes(Pageable pageable) {
        return customerNoteRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<CustomerNoteResponse> getNotesByCustomer(UUID customerId, Pageable pageable) {
        return customerNoteRepository.findByCustomerId(customerId, pageable).map(this::mapToResponse);
    }

    public Page<CustomerNoteResponse> getNotesByLead(UUID leadId, Pageable pageable) {
        return customerNoteRepository.findByLeadId(leadId, pageable).map(this::mapToResponse);
    }

    @Transactional
    public CustomerNoteResponse updateNote(Long id, CustomerNoteRequest request) {
        CustomerNote note = customerNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerNote not found with id: " + id));
        mapToEntity(request, note);
        note = customerNoteRepository.save(note);
        return mapToResponse(note);
    }

    @Transactional
    public void deleteNote(Long id) {
        CustomerNote note = customerNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerNote not found with id: " + id));
        note.setIsDeleted(true);
        customerNoteRepository.save(note);
    }

    private void mapToEntity(CustomerNoteRequest request, CustomerNote note) {
        note.setCustomerId(request.getCustomerId());
        note.setLeadId(request.getLeadId());
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setPinned(request.isPinned());
    }

    private CustomerNoteResponse mapToResponse(CustomerNote note) {
        return CustomerNoteResponse.builder()
                .id(note.getId())
                .customerId(note.getCustomerId())
                .leadId(note.getLeadId())
                .title(note.getTitle())
                .content(note.getContent())
                .isPinned(note.isPinned())
                .createdAt(note.getCreatedAt())
                .build();
    }
}
