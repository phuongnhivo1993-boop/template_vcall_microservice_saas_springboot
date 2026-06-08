package com.vcall.email.controller;

import com.vcall.email.dto.EmailRequest;
import com.vcall.email.dto.EmailResponse;
import com.vcall.email.service.EmailService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailService.sendEmail(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deleteEmail(@PathVariable UUID id) {
        emailService.deleteEmail(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<EmailResponse> getEmail(@PathVariable UUID id) {
        return ResponseEntity.ok(emailService.getEmail(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Page<EmailResponse>> getEmails(Pageable pageable) {
        return ResponseEntity.ok(emailService.getEmails(pageable));
    }

    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<EmailResponse>> getConversationThread(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(emailService.getConversationThread(conversationId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<EmailResponse>> searchEmails(@RequestParam("q") String query) {
        return ResponseEntity.ok(emailService.searchEmails(query));
    }
}
