package com.vcall.recording.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.recording.dto.RecordingRequest;
import com.vcall.recording.dto.RecordingResponse;
import com.vcall.recording.dto.RecordingSearchRequest;
import com.vcall.recording.entity.Recording;
import com.vcall.recording.kafka.RecordingEventPublisher;
import com.vcall.recording.repository.RecordingRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RecordingService {

    private final RecordingRepository recordingRepository;
    private final RecordingStorageService storageService;
    private final RecordingEventPublisher eventPublisher;

    @Transactional
    public RecordingResponse createRecording(RecordingRequest request) {
        Recording recording = new Recording();
        recording.setCallId(request.getCallId());
        recording.setAgentId(request.getAgentId());
        recording.setCustomerNumber(request.getCustomerNumber());
        recording.setFormat(Recording.RecordingFormat.valueOf(request.getFormat().toUpperCase()));
        recording.setStatus(Recording.RecordingStatus.RECORDING);
        recording.setStartedAt(LocalDateTime.now());
        recording.setFileName(request.getCallId() + "_" + System.currentTimeMillis() + "." + request.getFormat().toLowerCase());
        recording.setFilePath("recordings/" + recording.getFileName());
        recording = recordingRepository.save(recording);

        eventPublisher.publishCallRecorded(recording);
        return toResponse(recording);
    }

    @Transactional
    public RecordingResponse startRecording(UUID callId, UUID agentId, String customerNumber, String format) {
        Recording recording = new Recording();
        recording.setCallId(callId);
        recording.setAgentId(agentId);
        recording.setCustomerNumber(customerNumber);
        recording.setFormat(Recording.RecordingFormat.valueOf(format.toUpperCase()));
        recording.setStatus(Recording.RecordingStatus.RECORDING);
        recording.setStartedAt(LocalDateTime.now());
        recording.setFileName(callId + "_" + System.currentTimeMillis() + "." + format.toLowerCase());
        recording.setFilePath("recordings/" + recording.getFileName());
        recording = recordingRepository.save(recording);

        eventPublisher.publishCallRecorded(recording);
        return toResponse(recording);
    }

    @Transactional
    public RecordingResponse stopRecording(UUID id) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + id));
        recording.setStatus(Recording.RecordingStatus.COMPLETED);
        recording.setCompletedAt(LocalDateTime.now());
        recording = recordingRepository.save(recording);

        eventPublisher.publishRecordingCompleted(recording);
        return toResponse(recording);
    }

    @Transactional
    public RecordingResponse uploadFile(UUID id, MultipartFile file) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + id));
        storageService.uploadFile(recording.getFilePath(), file);
        recording.setFileSize(file.getSize());
        recording = recordingRepository.save(recording);
        return toResponse(recording);
    }

    @Transactional
    public RecordingResponse updateRecording(UUID id, RecordingRequest request) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + id));
        if (request.getCallId() != null) recording.setCallId(request.getCallId());
        if (request.getAgentId() != null) recording.setAgentId(request.getAgentId());
        if (request.getCustomerNumber() != null) recording.setCustomerNumber(request.getCustomerNumber());
        if (request.getFormat() != null) recording.setFormat(Recording.RecordingFormat.valueOf(request.getFormat().toUpperCase()));
        recording = recordingRepository.save(recording);

        eventPublisher.publishRecordingUpdated(recording);
        return toResponse(recording);
    }

    @Transactional(readOnly = true)
    public RecordingResponse getRecording(UUID id) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + id));
        return toResponse(recording);
    }

    @Transactional(readOnly = true)
    public Page<RecordingResponse> searchRecordings(RecordingSearchRequest request, Pageable pageable) {
        Specification<Recording> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getAgentId() != null) {
                predicates.add(cb.equal(root.get("agentId"), request.getAgentId()));
            }
            if (request.getCustomerNumber() != null && !request.getCustomerNumber().isEmpty()) {
                predicates.add(cb.equal(root.get("customerNumber"), request.getCustomerNumber()));
            }
            if (request.getFormat() != null && !request.getFormat().isEmpty()) {
                predicates.add(cb.equal(root.get("format"), request.getFormat()));
            }
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startedAt"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startedAt"), request.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return recordingRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public String getDownloadUrl(UUID id) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + id));
        URL url = storageService.getFileUrl(recording.getFilePath());
        return url.toString();
    }

    @Transactional
    public void deleteRecording(UUID id) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + id));
        storageService.deleteFile(recording.getFilePath());
        recording.setIsDeleted(true);
        recordingRepository.save(recording);

        eventPublisher.publishRecordingDeleted(recording);
    }

    private RecordingResponse toResponse(Recording recording) {
        String downloadUrl = null;
        try {
            downloadUrl = storageService.getFileUrl(recording.getFilePath()).toString();
        } catch (Exception e) {
            // ignore if URL generation fails
        }

        return RecordingResponse.builder()
                .id(recording.getId())
                .callId(recording.getCallId())
                .fileName(recording.getFileName())
                .fileSize(recording.getFileSize())
                .duration(recording.getDuration())
                .format(recording.getFormat().name())
                .status(recording.getStatus().name())
                .startedAt(recording.getStartedAt())
                .completedAt(recording.getCompletedAt())
                .agentId(recording.getAgentId())
                .customerNumber(recording.getCustomerNumber())
                .downloadUrl(downloadUrl)
                .build();
    }
}
