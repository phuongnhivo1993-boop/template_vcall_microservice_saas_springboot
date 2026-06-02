package com.vcall.recording.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.recording.dto.RetentionPolicyRequest;
import com.vcall.recording.dto.RetentionPolicyResponse;
import com.vcall.recording.entity.Recording;
import com.vcall.recording.entity.RetentionPolicy;
import com.vcall.recording.repository.RecordingRepository;
import com.vcall.recording.repository.RetentionPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetentionPolicyService {

    private final RetentionPolicyRepository retentionPolicyRepository;
    private final RecordingRepository recordingRepository;
    private final RecordingStorageService storageService;

    @Transactional
    public RetentionPolicyResponse createPolicy(RetentionPolicyRequest request) {
        RetentionPolicy policy = new RetentionPolicy();
        policy.setName(request.getName());
        policy.setDescription(request.getDescription());
        policy.setRetentionDays(request.getRetentionDays());
        policy.setAction(RetentionPolicy.RetentionAction.valueOf(request.getAction().toUpperCase()));
        policy.setActive(request.isActive());
        policy = retentionPolicyRepository.save(policy);
        return toResponse(policy);
    }

    @Transactional(readOnly = true)
    public RetentionPolicyResponse getPolicy(Long id) {
        RetentionPolicy policy = retentionPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retention policy not found with id: " + id));
        return toResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<RetentionPolicyResponse> getAllPolicies() {
        return retentionPolicyRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RetentionPolicyResponse> getAllPolicies(Pageable pageable) {
        return retentionPolicyRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public RetentionPolicyResponse updatePolicy(Long id, RetentionPolicyRequest request) {
        RetentionPolicy policy = retentionPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retention policy not found with id: " + id));
        policy.setName(request.getName());
        policy.setDescription(request.getDescription());
        policy.setRetentionDays(request.getRetentionDays());
        policy.setAction(RetentionPolicy.RetentionAction.valueOf(request.getAction().toUpperCase()));
        policy.setActive(request.isActive());
        policy = retentionPolicyRepository.save(policy);
        return toResponse(policy);
    }

    @Transactional
    public void deletePolicy(Long id) {
        RetentionPolicy policy = retentionPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retention policy not found with id: " + id));
        policy.setIsDeleted(true);
        retentionPolicyRepository.save(policy);
    }

    @Transactional
    public int applyRetention() {
        List<RetentionPolicy> activePolicies = retentionPolicyRepository.findByIsActiveTrue();
        int totalAffected = 0;

        for (RetentionPolicy policy : activePolicies) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(policy.getRetentionDays());
            List<Recording> expiredRecordings = recordingRepository.findByCompletedAtBefore(cutoffDate);

            for (Recording recording : expiredRecordings) {
                try {
                    switch (policy.getAction()) {
                        case DELETE:
                            storageService.deleteFile(recording.getFilePath());
                            recordingRepository.delete(recording);
                            log.info("Deleted recording {} due to retention policy '{}'", recording.getId(), policy.getName());
                            break;
                        case ARCHIVE:
                            recording.setStatus(Recording.RecordingStatus.PROCESSING);
                            recordingRepository.save(recording);
                            log.info("Archived recording {} due to retention policy '{}'", recording.getId(), policy.getName());
                            break;
                        case COMPRESS:
                            recording.setStatus(Recording.RecordingStatus.PROCESSING);
                            recordingRepository.save(recording);
                            log.info("Compressed recording {} due to retention policy '{}'", recording.getId(), policy.getName());
                            break;
                    }
                    totalAffected++;
                } catch (Exception e) {
                    log.error("Failed to apply retention policy {} on recording {}: {}",
                            policy.getId(), recording.getId(), e.getMessage());
                }
            }
        }

        return totalAffected;
    }

    private RetentionPolicyResponse toResponse(RetentionPolicy policy) {
        return RetentionPolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .retentionDays(policy.getRetentionDays())
                .action(policy.getAction().name())
                .isActive(policy.isActive())
                .build();
    }
}
