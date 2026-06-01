package com.vcall.sipservice.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.sipservice.dto.SipAccountRequest;
import com.vcall.sipservice.dto.SipAccountResponse;
import com.vcall.sipservice.entity.SipAccount;
import com.vcall.sipservice.entity.SipRegistration;
import com.vcall.sipservice.kafka.SipEventPublisher;
import com.vcall.sipservice.repository.SipAccountRepository;
import com.vcall.sipservice.repository.SipDeviceRepository;
import com.vcall.sipservice.repository.SipRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SipAccountService {

    private final SipAccountRepository sipAccountRepository;
    private final SipDeviceRepository sipDeviceRepository;
    private final SipRegistrationRepository sipRegistrationRepository;
    private final SipEventPublisher sipEventPublisher;

    @Transactional(readOnly = true)
    public List<SipAccountResponse> findAll() {
        return sipAccountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SipAccountResponse findById(Long id) {
        SipAccount account = sipAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipAccount not found: " + id));
        return toResponse(account);
    }

    @Transactional
    public SipAccountResponse create(SipAccountRequest request) {
        if (sipAccountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("SipAccount already exists: " + request.getUsername());
        }

        SipAccount account = new SipAccount();
        account.setUsername(request.getUsername());
        account.setPassword(request.getPassword());
        account.setDomain(request.getDomain());
        account.setRealm(request.getRealm());
        account.setAccountType(SipAccount.AccountType.valueOf(request.getAccountType()));
        account.setStatus(SipAccount.AccountStatus.ACTIVE);
        account.setMaxChannels(request.getMaxChannels() != null ? request.getMaxChannels() : 10);
        account.setAllowRegistration(request.getAllowRegistration() != null ? request.getAllowRegistration() : true);
        account.setTenantId(request.getTenantId());

        SipAccount saved = sipAccountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional
    public SipAccountResponse update(Long id, SipAccountRequest request) {
        SipAccount account = sipAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipAccount not found: " + id));

        if (!account.getUsername().equals(request.getUsername())
                && sipAccountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("SipAccount already exists: " + request.getUsername());
        }

        account.setUsername(request.getUsername());
        account.setPassword(request.getPassword());
        account.setDomain(request.getDomain());
        account.setRealm(request.getRealm());
        account.setAccountType(SipAccount.AccountType.valueOf(request.getAccountType()));
        account.setMaxChannels(request.getMaxChannels() != null ? request.getMaxChannels() : 10);
        account.setAllowRegistration(request.getAllowRegistration() != null ? request.getAllowRegistration() : true);
        account.setTenantId(request.getTenantId());

        SipAccount saved = sipAccountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        SipAccount account = sipAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipAccount not found: " + id));
        sipAccountRepository.delete(account);
    }

    @Transactional
    public SipAccountResponse updateStatus(Long id, String status) {
        SipAccount account = sipAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipAccount not found: " + id));
        account.setStatus(SipAccount.AccountStatus.valueOf(status.toUpperCase()));
        SipAccount saved = sipAccountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SipAccountResponse> findByTenantId(java.util.UUID tenantId) {
        return sipAccountRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void register(Long id) {
        SipAccount account = sipAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipAccount not found: " + id));
        account.setStatus(SipAccount.AccountStatus.ACTIVE);
        sipAccountRepository.save(account);
        sipEventPublisher.publishAccountRegistered(account);
    }

    @Transactional
    public void unregister(Long id) {
        SipAccount account = sipAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipAccount not found: " + id));
        account.setStatus(SipAccount.AccountStatus.INACTIVE);
        sipAccountRepository.save(account);

        List<SipRegistration> registrations = sipRegistrationRepository.findBySipAccountId(id);
        for (SipRegistration reg : registrations) {
            reg.setStatus(SipRegistration.RegistrationStatus.UNREGISTERED);
            sipRegistrationRepository.save(reg);
        }
        sipEventPublisher.publishAccountUnregistered(account);
    }

    @Transactional
    public void checkExpired() {
        List<SipRegistration> expiredRegistrations = sipRegistrationRepository
                .findByExpiresBefore(LocalDateTime.now());
        for (SipRegistration reg : expiredRegistrations) {
            if (reg.getStatus() == SipRegistration.RegistrationStatus.REGISTERED) {
                reg.setStatus(SipRegistration.RegistrationStatus.EXPIRED);
                sipRegistrationRepository.save(reg);
            }
        }
    }

    private SipAccountResponse toResponse(SipAccount account) {
        long deviceCount = sipDeviceRepository.findBySipAccountId(account.getId()).size();
        return SipAccountResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .domain(account.getDomain())
                .realm(account.getRealm())
                .accountType(account.getAccountType().name())
                .status(account.getStatus().name())
                .maxChannels(account.getMaxChannels())
                .allowRegistration(account.getAllowRegistration())
                .tenantId(account.getTenantId())
                .devices(deviceCount)
                .build();
    }
}
