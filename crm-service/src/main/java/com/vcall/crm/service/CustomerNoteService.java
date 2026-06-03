package com.vcall.crm.service;

import com.vcall.audit.entity.AuditLog;
import com.vcall.audit.entity.AuditLog.Action;
import com.vcall.audit.entity.AuditLog.ActorType;
import com.vcall.audit.entity.AuditLog.AuditStatus;
import com.vcall.audit.service.AuditLogService;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.crm.dto.CustomerNoteRequest;
import com.vcall.crm.dto.CustomerNoteResponse;
import com.vcall.crm.entity.CustomerNote;
import com.vcall.crm.repository.CustomerNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerNoteService {

    private final CustomerNoteRepository customerNoteRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerNoteResponse createNote(CustomerNoteRequest request) {
        CustomerNote note = new CustomerNote();
        mapToEntity(request, note);
        note = customerNoteRepository.save(note);
        
        // Audit log
        AuditLog auditLog = AuditLog.builder()
                .actorId(UUID.randomUUID())
                .actorType(ActorType.USER)
                .action(Action.CREATE)
                .resource("CustomerNote")
                .resourceId(note.getId().toString())
                .resourceType("CustomerNote")
                .details("Created customer note: " + note.getTitle())
                .status(AuditStatus.SUCCESS)
                .build();
        auditLogService.createLog(auditLog);
        
        return mapToResponse(note);
    }

    public CustomerNoteResponse getNote(Long id) {
        CustomerNote note = customerNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerNote not found with id: " + id));
        
        // Audit log
        AuditLog auditLog = AuditLog.builder()
                .actorId(UUID.randomUUID())
                .actorType(ActorType.USER)
                .action(Action.READ)
                .resource("CustomerNote")
                .resourceId(note.getId().toString())
                .resourceType("CustomerNote")
                .details("Retrieved customer note: " + note.getTitle())
                .status(AuditStatus.SUCCESS)
                .build();
        auditLogService.createLog(auditLog);
        
        return mapToResponse(note);
    }

    public Page<CustomerNoteResponse> getAllNotes(Pageable pageable) {
        return customerNoteRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<CustomerNoteResponse> searchNotes(Specification<CustomerNote> spec, Pageable pageable) {
        return customerNoteRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public Map<String, Object> getNoteStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNotes", customerNoteRepository.count());
        return stats;
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
        
        // Audit log
        AuditLog auditLog = AuditLog.builder()
                .actorId(UUID.randomUUID())
                .actorType(ActorType.USER)
                .action(Action.UPDATE)
                .resource("CustomerNote")
                .resourceId(note.getId().toString())
                .resourceType("CustomerNote")
                .details("Updated customer note: " + note.getTitle())
                .status(AuditStatus.SUCCESS)
                .build();
        auditLogService.createLog(auditLog);
        
        return mapToResponse(note);
    }

    @Transactional
    public void deleteNote(Long id) {
        CustomerNote note = customerNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerNote not found with id: " + id));
        note.setIsDeleted(true);
        customerNoteRepository.save(note);
        
        // Audit log
        AuditLog auditLog = AuditLog.builder()
                .actorId(UUID.randomUUID())
                .actorType(ActorType.USER)
                .action(Action.DELETE)
                .resource("CustomerNote")
                .resourceId(note.getId().toString())
                .resourceType("CustomerNote")
                .details("Deleted customer note: " + note.getTitle())
                .status(AuditStatus.SUCCESS)
                .build();
        auditLogService.createLog(auditLog);
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
