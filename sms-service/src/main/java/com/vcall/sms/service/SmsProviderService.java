package com.vcall.sms.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.sms.dto.SmsProviderRequest;
import com.vcall.sms.dto.SmsProviderResponse;
import com.vcall.sms.entity.SmsProvider;
import com.vcall.sms.repository.SmsProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsProviderService {

    private final SmsProviderRepository smsProviderRepository;

    @Transactional
    public SmsProviderResponse createProvider(SmsProviderRequest request) {
        if (smsProviderRepository.findByProviderType(SmsProvider.ProviderType.valueOf(request.getProviderType()))
                .stream().anyMatch(p -> p.getName().equals(request.getName()))) {
            throw new DuplicateResourceException("Provider already exists with name: " + request.getName());
        }

        if (request.isDefault()) {
            resetDefaultProvider();
        }

        SmsProvider provider = new SmsProvider();
        provider.setName(request.getName());
        provider.setProviderType(SmsProvider.ProviderType.valueOf(request.getProviderType()));
        provider.setApiUrl(request.getApiUrl());
        provider.setApiKey(request.getApiKey());
        provider.setApiSecret(request.getApiSecret());
        provider.setSenderId(request.getSenderId());
        provider.setDefault(request.isDefault());
        provider.setActive(request.isActive());

        provider = smsProviderRepository.save(provider);
        return toResponse(provider);
    }

    public SmsProviderResponse getProvider(Long id) {
        SmsProvider provider = smsProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));
        return toResponse(provider);
    }

    public List<SmsProviderResponse> getAllProviders() {
        return smsProviderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SmsProviderResponse> getActiveProviders() {
        return smsProviderRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SmsProviderResponse> getProvidersByType(String providerType) {
        return smsProviderRepository.findByProviderType(SmsProvider.ProviderType.valueOf(providerType)).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SmsProviderResponse getDefaultProvider() {
        SmsProvider provider = smsProviderRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No default provider configured"));
        return toResponse(provider);
    }

    @Transactional
    public SmsProviderResponse updateProvider(Long id, SmsProviderRequest request) {
        SmsProvider provider = smsProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));

        if (request.isDefault()) {
            resetDefaultProvider();
        }

        provider.setName(request.getName());
        provider.setProviderType(SmsProvider.ProviderType.valueOf(request.getProviderType()));
        provider.setApiUrl(request.getApiUrl());
        provider.setApiKey(request.getApiKey());
        provider.setApiSecret(request.getApiSecret());
        provider.setSenderId(request.getSenderId());
        provider.setDefault(request.isDefault());
        provider.setActive(request.isActive());

        provider = smsProviderRepository.save(provider);
        return toResponse(provider);
    }

    @Transactional
    public void deleteProvider(Long id) {
        SmsProvider provider = smsProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));
        smsProviderRepository.delete(provider);
    }

    public boolean testProvider(Long id) {
        SmsProvider provider = smsProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));
        // Integration point: test connectivity to provider API
        log.info("Testing provider: {} at {}", provider.getName(), provider.getApiUrl());
        return true;
    }

    private void resetDefaultProvider() {
        smsProviderRepository.findByIsDefaultTrue().ifPresent(existing -> {
            existing.setDefault(false);
            smsProviderRepository.save(existing);
        });
    }

    private SmsProviderResponse toResponse(SmsProvider provider) {
        return SmsProviderResponse.builder()
                .id(provider.getId())
                .name(provider.getName())
                .providerType(provider.getProviderType().name())
                .senderId(provider.getSenderId())
                .isDefault(provider.isDefault())
                .isActive(provider.isActive())
                .build();
    }
}
