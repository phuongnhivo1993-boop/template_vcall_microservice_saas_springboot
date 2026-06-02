package com.vcall.pbx.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.pbx.dto.ExtensionRequest;
import com.vcall.pbx.dto.ExtensionResponse;
import com.vcall.pbx.entity.Extension;
import com.vcall.pbx.entity.Extension.ExtensionStatus;
import com.vcall.pbx.entity.Extension.ExtensionType;
import com.vcall.pbx.kafka.PbxEventPublisher;
import com.vcall.pbx.repository.ExtensionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExtensionService {

    private final ExtensionRepository extensionRepository;
    private final PbxEventPublisher eventPublisher;

    @Transactional
    public ExtensionResponse createExtension(ExtensionRequest request) {
        if (extensionRepository.findByExtensionNumber(request.getExtensionNumber()).isPresent()) {
            throw new DuplicateResourceException("Extension already exists with number: " + request.getExtensionNumber());
        }

        Extension extension = new Extension();
        extension.setExtensionNumber(request.getExtensionNumber());
        extension.setPassword(request.getPassword());
        extension.setDisplayName(request.getDisplayName());
        extension.setType(ExtensionType.valueOf(request.getType().toUpperCase()));
        extension.setStatus(ExtensionStatus.ACTIVE);
        extension.setVoicemailEnabled(request.getVoicemailEnabled() != null ? request.getVoicemailEnabled() : true);
        extension.setCallForwarding(request.getCallForwarding());
        extension.setOutboundCallerId(request.getOutboundCallerId());
        extension.setMaxConcurrentCalls(request.getMaxConcurrentCalls() != null ? request.getMaxConcurrentCalls() : 6);
        extension = extensionRepository.save(extension);

        eventPublisher.publishExtensionCreated(extension);
        return toResponse(extension);
    }

    @Transactional(readOnly = true)
    public ExtensionResponse getExtension(Long id) {
        Extension extension = extensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found with id: " + id));
        return toResponse(extension);
    }

    @Transactional(readOnly = true)
    public Page<ExtensionResponse> getAllExtensions(Pageable pageable) {
        return extensionRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ExtensionResponse updateExtension(Long id, ExtensionRequest request) {
        Extension extension = extensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found with id: " + id));
        extension.setDisplayName(request.getDisplayName());
        extension.setPassword(request.getPassword());
        if (request.getType() != null) {
            extension.setType(ExtensionType.valueOf(request.getType().toUpperCase()));
        }
        extension.setVoicemailEnabled(request.getVoicemailEnabled());
        extension.setCallForwarding(request.getCallForwarding());
        extension.setOutboundCallerId(request.getOutboundCallerId());
        extension.setMaxConcurrentCalls(request.getMaxConcurrentCalls());
        extension = extensionRepository.save(extension);
        return toResponse(extension);
    }

    @Transactional
    public void deleteExtension(Long id) {
        Extension extension = extensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found with id: " + id));
        extension.setIsDeleted(true);
        extensionRepository.save(extension);
    }

    @Transactional(readOnly = true)
    public Page<ExtensionResponse> getByStatus(String status, Pageable pageable) {
        ExtensionStatus statusEnum = ExtensionStatus.valueOf(status.toUpperCase());
        return extensionRepository.findByStatus(statusEnum, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ExtensionResponse> getBySipAccount(Long sipAccountId, Pageable pageable) {
        return extensionRepository.findBySipAccountId(sipAccountId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ExtensionResponse updateStatus(Long id, String status) {
        Extension extension = extensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found with id: " + id));
        ExtensionStatus newStatus = ExtensionStatus.valueOf(status.toUpperCase());
        extension.setStatus(newStatus);
        extension = extensionRepository.save(extension);

        eventPublisher.publishExtensionStatusChanged(extension);
        return toResponse(extension);
    }

    @Transactional(readOnly = true)
    public Page<ExtensionResponse> search(String keyword, String status, String type, Pageable pageable) {
        Specification<Extension> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("extensionNumber")), pattern),
                        cb.like(cb.lower(root.get("displayName")), pattern),
                        cb.like(cb.lower(root.get("outboundCallerId")), pattern)
                ));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), ExtensionStatus.valueOf(status.toUpperCase())));
            }
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("type"), ExtensionType.valueOf(type.toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return extensionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        List<Extension> all = extensionRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) all.size());
        stats.put("active", all.stream().filter(e -> e.getStatus() == ExtensionStatus.ACTIVE).count());
        stats.put("inactive", all.stream().filter(e -> e.getStatus() == ExtensionStatus.INACTIVE).count());
        stats.put("busy", all.stream().filter(e -> e.getStatus() == ExtensionStatus.BUSY).count());
        return stats;
    }

    @Transactional
    public ExtensionResponse setCallForwarding(Long id, String callForwarding) {
        Extension extension = extensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found with id: " + id));
        extension.setCallForwarding(callForwarding);
        extension = extensionRepository.save(extension);
        return toResponse(extension);
    }

    private ExtensionResponse toResponse(Extension extension) {
        return ExtensionResponse.builder()
                .id(extension.getId())
                .extensionNumber(extension.getExtensionNumber())
                .displayName(extension.getDisplayName())
                .type(extension.getType().name())
                .status(extension.getStatus().name())
                .voicemailEnabled(extension.getVoicemailEnabled())
                .callForwarding(extension.getCallForwarding())
                .outboundCallerId(extension.getOutboundCallerId())
                .maxConcurrentCalls(extension.getMaxConcurrentCalls())
                .createdAt(extension.getCreatedAt())
                .build();
    }
}
