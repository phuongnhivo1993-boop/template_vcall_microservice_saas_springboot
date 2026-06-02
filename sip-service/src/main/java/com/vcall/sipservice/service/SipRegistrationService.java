package com.vcall.sipservice.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.sipservice.dto.SipRegistrationResponse;
import com.vcall.sipservice.entity.SipAccount;
import com.vcall.sipservice.entity.SipRegistration;
import com.vcall.sipservice.repository.SipAccountRepository;
import com.vcall.sipservice.repository.SipRegistrationRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Transactional(readOnly = true)
    public Page<SipRegistrationResponse> findAll(Pageable pageable) {
        return sipRegistrationRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SipRegistrationResponse> search(String keyword, String status, Pageable pageable) {
        Specification<SipRegistration> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("ipAddress")), pattern),
                        cb.like(cb.lower(root.get("userAgent")), pattern),
                        cb.like(cb.lower(root.get("contactUri")), pattern)
                ));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), SipRegistration.RegistrationStatus.valueOf(status.toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return sipRegistrationRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        List<SipRegistration> all = sipRegistrationRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) all.size());
        stats.put("registered", all.stream().filter(r -> r.getStatus() == SipRegistration.RegistrationStatus.REGISTERED).count());
        stats.put("expired", all.stream().filter(r -> r.getStatus() == SipRegistration.RegistrationStatus.EXPIRED).count());
        stats.put("unregistered", all.stream().filter(r -> r.getStatus() == SipRegistration.RegistrationStatus.UNREGISTERED).count());
        return stats;
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
