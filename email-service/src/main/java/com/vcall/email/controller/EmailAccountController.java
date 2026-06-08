package com.vcall.email.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.email.dto.EmailAccountRequest;
import com.vcall.email.dto.EmailAccountResponse;
import com.vcall.email.service.EmailAccountService;
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
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/email/accounts")
@RequiredArgsConstructor
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<EmailAccountResponse> createAccount(@Valid @RequestBody EmailAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailAccountService.createAccount(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<EmailAccountResponse> updateAccount(@PathVariable Long id,
                                                               @Valid @RequestBody EmailAccountRequest request) {
        return ResponseEntity.ok(emailAccountService.updateAccount(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<EmailAccountResponse> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(emailAccountService.getAccount(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<EmailAccountResponse>>> getAllAccounts(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(emailAccountService.getAllAccounts(pageable)));
    }

    @PutMapping("/{id}/default")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<EmailAccountResponse> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(emailAccountService.setDefault(id));
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Boolean> testConnection(@PathVariable Long id) {
        return ResponseEntity.ok(emailAccountService.testConnection(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        emailAccountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
