package com.vcall.notification.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.notification.dto.PushDeviceRequest;
import com.vcall.notification.dto.PushDeviceResponse;
import com.vcall.notification.entity.PushDevice;
import com.vcall.notification.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushDeviceService {

    private final PushDeviceRepository pushDeviceRepository;

    @Transactional
    public PushDeviceResponse registerDevice(PushDeviceRequest request) {
        if (pushDeviceRepository.findByDeviceToken(request.getDeviceToken()).isPresent()) {
            throw new DuplicateResourceException("Device token already registered: " + request.getDeviceToken());
        }
        PushDevice device = new PushDevice();
        device.setUserId(request.getUserId());
        device.setDeviceToken(request.getDeviceToken());
        device.setPlatform(request.getPlatform());
        device.setAppVersion(request.getAppVersion());
        device.setDeviceModel(request.getDeviceModel());
        device.setIsActive(true);
        device.setLastUsedAt(LocalDateTime.now());
        device = pushDeviceRepository.save(device);
        return toResponse(device);
    }

    @Transactional
    public void unregisterDevice(Long id) {
        PushDevice device = pushDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Push device not found with id: " + id));
        pushDeviceRepository.delete(device);
    }

    @Transactional(readOnly = true)
    public List<PushDeviceResponse> getActiveDevices(UUID userId) {
        return pushDeviceRepository.findByUserId(userId)
                .stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsActive()))
                .map(this::toResponse)
                .toList();
    }

    private PushDeviceResponse toResponse(PushDevice device) {
        return PushDeviceResponse.builder()
                .id(device.getId())
                .userId(device.getUserId())
                .deviceToken(device.getDeviceToken())
                .platform(device.getPlatform().name())
                .isActive(device.getIsActive())
                .build();
    }
}
