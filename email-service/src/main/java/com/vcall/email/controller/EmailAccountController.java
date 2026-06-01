package com.vcall.email.controller;

import com.vcall.email.dto.EmailAccountRequest;
import com.vcall.email.dto.EmailAccountResponse;
import com.vcall.email.service.EmailAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/email/accounts")
@RequiredArgsConstructor
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    @PostMapping
    public ResponseEntity<EmailAccountResponse> createAccount(@Valid @RequestBody EmailAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailAccountService.createAccount(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailAccountResponse> updateAccount(@PathVariable Long id,
                                                               @Valid @RequestBody EmailAccountRequest request) {
        return ResponseEntity.ok(emailAccountService.updateAccount(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailAccountResponse> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(emailAccountService.getAccount(id));
    }

    @GetMapping
    public ResponseEntity<List<EmailAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(emailAccountService.getAllAccounts());
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<EmailAccountResponse> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(emailAccountService.setDefault(id));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<Boolean> testConnection(@PathVariable Long id) {
        return ResponseEntity.ok(emailAccountService.testConnection(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        emailAccountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
