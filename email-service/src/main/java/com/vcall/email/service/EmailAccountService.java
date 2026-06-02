package com.vcall.email.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.email.dto.EmailAccountRequest;
import com.vcall.email.dto.EmailAccountResponse;
import com.vcall.email.entity.EmailAccount;
import com.vcall.email.repository.EmailAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;

    @Transactional
    public EmailAccountResponse createAccount(EmailAccountRequest request) {
        if (emailAccountRepository.findByEmailAddress(request.getEmailAddress()).isPresent()) {
            throw new DuplicateResourceException("Account already exists with email: " + request.getEmailAddress());
        }

        EmailAccount account = new EmailAccount();
        account.setEmailAddress(request.getEmailAddress());
        account.setDisplayName(request.getDisplayName());
        account.setSmtpHost(request.getSmtpHost());
        account.setSmtpPort(request.getSmtpPort());
        account.setSmtpUsername(request.getSmtpUsername());
        account.setSmtpPassword(request.getSmtpPassword());
        account.setImapHost(request.getImapHost());
        account.setImapPort(request.getImapPort());
        account.setUseSSL(request.getUseSSL() != null ? request.getUseSSL() : true);
        account.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        if (Boolean.TRUE.equals(account.getIsDefault())) {
            clearDefaultFlag();
        }

        account = emailAccountRepository.save(account);
        return toResponse(account);
    }

    @Transactional
    public EmailAccountResponse updateAccount(Long id, EmailAccountRequest request) {
        EmailAccount account = emailAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        account.setEmailAddress(request.getEmailAddress());
        account.setDisplayName(request.getDisplayName());
        account.setSmtpHost(request.getSmtpHost());
        account.setSmtpPort(request.getSmtpPort());
        account.setSmtpUsername(request.getSmtpUsername());
        account.setSmtpPassword(request.getSmtpPassword());
        account.setImapHost(request.getImapHost());
        account.setImapPort(request.getImapPort());
        account.setUseSSL(request.getUseSSL() != null ? request.getUseSSL() : account.getUseSSL());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultFlag();
            account.setIsDefault(true);
        }

        account = emailAccountRepository.save(account);
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public EmailAccountResponse getAccount(Long id) {
        EmailAccount account = emailAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<EmailAccountResponse> getAllAccounts() {
        return emailAccountRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAccount(Long id) {
        EmailAccount account = emailAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        account.setIsDeleted(true);
        emailAccountRepository.save(account);
    }

    @Transactional
    public EmailAccountResponse setDefault(Long id) {
        EmailAccount account = emailAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        clearDefaultFlag();
        account.setIsDefault(true);
        account = emailAccountRepository.save(account);
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public boolean testConnection(Long id) {
        EmailAccount account = emailAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        // Placeholder for actual connection test logic
        return account.getSmtpHost() != null;
    }

    private void clearDefaultFlag() {
        emailAccountRepository.findByIsDefaultTrue()
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    emailAccountRepository.save(existing);
                });
    }

    private EmailAccountResponse toResponse(EmailAccount account) {
        return EmailAccountResponse.builder()
                .id(account.getId())
                .emailAddress(account.getEmailAddress())
                .displayName(account.getDisplayName())
                .smtpHost(account.getSmtpHost())
                .smtpPort(account.getSmtpPort())
                .isDefault(account.getIsDefault())
                .build();
    }
}
