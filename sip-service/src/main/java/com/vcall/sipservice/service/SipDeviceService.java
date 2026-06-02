package com.vcall.sipservice.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.sipservice.dto.SipDeviceRequest;
import com.vcall.sipservice.dto.SipDeviceResponse;
import com.vcall.sipservice.entity.SipDevice;
import com.vcall.sipservice.repository.SipAccountRepository;
import com.vcall.sipservice.repository.SipDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SipDeviceService {

    private final SipDeviceRepository sipDeviceRepository;
    private final SipAccountRepository sipAccountRepository;

    @Transactional(readOnly = true)
    public Page<SipDeviceResponse> findAll(Pageable pageable) {
        return sipDeviceRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SipDeviceResponse findById(Long id) {
        SipDevice device = sipDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipDevice not found: " + id));
        return toResponse(device);
    }

    @Transactional
    public SipDeviceResponse create(SipDeviceRequest request, Long sipAccountId) {
        SipDevice device = new SipDevice();
        device.setName(request.getName());
        device.setDeviceType(SipDevice.DeviceType.valueOf(request.getDeviceType()));
        device.setUserAgent(request.getUserAgent());
        device.setIpAddress(request.getIpAddress());
        device.setMacAddress(request.getMacAddress());
        device.setFirmwareVersion(request.getFirmwareVersion());
        device.setSipAccount(sipAccountRepository.findById(sipAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("SipAccount not found: " + sipAccountId)));

        SipDevice saved = sipDeviceRepository.save(device);
        return toResponse(saved);
    }

    @Transactional
    public SipDeviceResponse update(Long id, SipDeviceRequest request) {
        SipDevice device = sipDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipDevice not found: " + id));

        device.setName(request.getName());
        device.setDeviceType(SipDevice.DeviceType.valueOf(request.getDeviceType()));
        device.setUserAgent(request.getUserAgent());
        device.setIpAddress(request.getIpAddress());
        device.setMacAddress(request.getMacAddress());
        device.setFirmwareVersion(request.getFirmwareVersion());

        SipDevice saved = sipDeviceRepository.save(device);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        SipDevice device = sipDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SipDevice not found: " + id));
        device.setIsDeleted(true);
        sipDeviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public Page<SipDeviceResponse> findByAccountId(Long sipAccountId, Pageable pageable) {
        return sipDeviceRepository.findBySipAccountId(sipAccountId, pageable)
                .map(this::toResponse);
    }

    private SipDeviceResponse toResponse(SipDevice device) {
        return SipDeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .deviceType(device.getDeviceType().name())
                .userAgent(device.getUserAgent())
                .ipAddress(device.getIpAddress())
                .macAddress(device.getMacAddress())
                .firmwareVersion(device.getFirmwareVersion())
                .build();
    }
}
