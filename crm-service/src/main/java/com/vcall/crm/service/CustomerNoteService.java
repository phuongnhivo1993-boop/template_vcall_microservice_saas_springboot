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

import java.util.List;
import java.util.UUID;

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

    public List<CustomerNoteResponse> getAllNotes() {
        return customerNoteRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CustomerNoteResponse> getNotesByCustomer(UUID customerId) {
        return customerNoteRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CustomerNoteResponse> getNotesByLead(UUID leadId) {
        return customerNoteRepository.findByLeadId(leadId).stream()
                .map(this::mapToResponse)
                .toList();
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
        customerNoteRepository.delete(note);
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
