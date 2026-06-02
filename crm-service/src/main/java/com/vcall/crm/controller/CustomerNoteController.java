package com.vcall.crm.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.crm.dto.CustomerNoteRequest;
import com.vcall.crm.dto.CustomerNoteResponse;
import com.vcall.crm.service.CustomerNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/notes")
@RequiredArgsConstructor
public class CustomerNoteController {

    private final CustomerNoteService customerNoteService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerNoteResponse>> createNote(@Valid @RequestBody CustomerNoteRequest request) {
        CustomerNoteResponse response = customerNoteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Note created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerNoteResponse>> getNote(@PathVariable Long id) {
        CustomerNoteResponse response = customerNoteService.getNote(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerNoteResponse>>> getAllNotes(Pageable pageable) {
        Page<CustomerNoteResponse> responses = customerNoteService.getAllNotes(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<CustomerNoteResponse>>> getNotesByCustomer(
            @PathVariable UUID customerId, Pageable pageable) {
        Page<CustomerNoteResponse> responses = customerNoteService.getNotesByCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerNoteResponse>> updateNote(@PathVariable Long id, @Valid @RequestBody CustomerNoteRequest request) {
        CustomerNoteResponse response = customerNoteService.updateNote(id, request);
        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(@PathVariable Long id) {
        customerNoteService.deleteNote(id);
        return ResponseEntity.ok(ApiResponse.success("Note deleted successfully", null));
    }
}
