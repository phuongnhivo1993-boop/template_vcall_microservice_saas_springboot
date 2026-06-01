package com.vcall.sipservice.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.sipservice.dto.SipRegistrationResponse;
import com.vcall.sipservice.entity.SipAccount;
import com.vcall.sipservice.entity.SipRegistration;
import com.vcall.sipservice.repository.SipAccountRepository;
import com.vcall.sipservice.repository.SipRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SipRegistrationService {

    private final SipRegistrationRepository sipRegistrationRepository;
    private final SipAccountRepository sipAccountRepository;

    @Transactional
    public SipRegistrationResponse register(Long sipAccountId, String contactUri, String userAgent,
                                            String ipAddress, Integer port, String transport,
                                            Integer expires) {
        SipAccount account = sipAccountRepository.findById(sipAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("SipAccount not found: " + sipAccountId));

        if (Boolean.FALSE.equals(account.getAllowRegistration())) {
            throw new IllegalStateException("Registration not allowed for account: " + account.getUsername());
        }

        SipRegistration registration = new SipRegistration();
        registration.setSipAccount(account);
        registration.setContactUri(contactUri);
        registration.setUserAgent(userAgent);
        registration.setIpAddress(ipAddress);
        registration.setPort(port);
        registration.setTransport(SipRegistration.Transport.valueOf(transport));
        registration.setExpires(expires);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setLastRefresh(LocalDateTime.now());
        registration.setStatus(SipRegistration.RegistrationStatus.REGISTERED);

        SipRegistration saved = sipRegistrationRepository.save(registration);
        return toResponse(saved);
    }

    @Transactional
    public SipRegistrationResponse refresh(Long id) {
        SipRegistration registration = sipRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipRegistration not found: " + id));

        registration.setLastRefresh(LocalDateTime.now());
        registration.setStatus(SipRegistration.RegistrationStatus.REGISTERED);

        SipRegistration saved = sipRegistrationRepository.save(registration);
        return toResponse(saved);
    }

    @Transactional
    public void unregister(Long id) {
        SipRegistration registration = sipRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipRegistration not found: " + id));

        registration.setStatus(SipRegistration.RegistrationStatus.UNREGISTERED);
        registration.setLastRefresh(LocalDateTime.now());
        sipRegistrationRepository.save(registration);
    }

    @Transactional
    public void cleanupExpired() {
        List<SipRegistration> expired = sipRegistrationRepository
                .findByExpiresBefore(LocalDateTime.now());
        for (SipRegistration reg : expired) {
            if (reg.getStatus() == SipRegistration.RegistrationStatus.REGISTERED) {
                reg.setStatus(SipRegistration.RegistrationStatus.EXPIRED);
                sipRegistrationRepository.save(reg);
            }
        }
    }

    @Transactional(readOnly = true)
    public SipRegistrationResponse findById(Long id) {
        SipRegistration registration = sipRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipRegistration not found: " + id));
        return toResponse(registration);
    }

    @Transactional(readOnly = true)
    public List<SipRegistrationResponse> findByAccountId(Long sipAccountId) {
        return sipRegistrationRepository.findBySipAccountId(sipAccountId).stream()
                .map(this::toResponse)
                .toList();
    }

    private SipRegistrationResponse toResponse(SipRegistration registration) {
        return SipRegistrationResponse.builder()
                .id(registration.getId())
                .sipAccountId(registration.getSipAccount().getId())
                .contactUri(registration.getContactUri())
                .userAgent(registration.getUserAgent())
                .ipAddress(registration.getIpAddress())
                .port(registration.getPort())
                .transport(registration.getTransport() != null ? registration.getTransport().name() : null)
                .registeredAt(registration.getRegisteredAt())
                .lastRefresh(registration.getLastRefresh())
                .status(registration.getStatus().name())
                .build();
    }
}
