package com.vcall.xr.video.service;

import com.vcall.xr.video.domain.VideoJob;
import com.vcall.xr.video.domain.VideoType;
import com.vcall.xr.video.repository.VideoJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VideoService {

    private final VideoJobRepository videoJobRepository;
    private final TranscodingService transcodingService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${video.processing.max-concurrent-jobs:5}")
    private int maxConcurrentJobs;

    private static final String TOPIC_VIDEO_EVENTS = "xr.video.events";

    public VideoJob createJob(UUID tenantId, String assetId, String inputUrl,
                              VideoType videoType, List<String> targetResolutions) {
        log.info("Creating video job for tenant {} asset {} type {}", tenantId, assetId, videoType);

        VideoJob job = VideoJob.builder()
                .tenantId(tenantId)
                .assetId(assetId)
                .inputUrl(inputUrl)
                .videoType(videoType)
                .targetResolutions(targetResolutions)
                .status(VideoJob.JobStatus.PENDING)
                .progress(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        VideoJob saved = videoJobRepository.save(job);
        log.info("Video job created with ID: {}", saved.getId());

        publishEvent(saved, "CREATED");
        return saved;
    }

    public VideoJob getJob(UUID id, UUID tenantId) {
        return videoJobRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new VideoNotFoundException("Video job not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<VideoJob> getJobsByTenant(UUID tenantId) {
        return videoJobRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public long getActiveJobCount(UUID tenantId) {
        return videoJobRepository.countActiveProcessingJobs(tenantId);
    }

    public VideoJob processJob(UUID id, UUID tenantId) {
        VideoJob job = getJob(id, tenantId);

        if (job.getStatus() != VideoJob.JobStatus.PENDING) {
            throw new IllegalStateException("Job is not in PENDING state: " + job.getStatus());
        }

        long activeJobs = videoJobRepository.countActiveProcessingJobs(tenantId);
        if (activeJobs >= maxConcurrentJobs) {
            throw new IllegalStateException("Maximum concurrent jobs reached for tenant: " + tenantId);
        }

        job.markProcessing();
        VideoJob saved = videoJobRepository.save(job);

        publishEvent(saved, "PROCESSING");

        transcodingService.transcodeAsync(saved, saved.getTargetResolutions(), new VideoJob.ProgressCallback() {
            @Override
            public void updateProgress(int progress) {
                saved.setProgress(progress);
                videoJobRepository.save(saved);
            }

            @Override
            public void onCompleted(String outputUrl) {
                saved.markCompleted(outputUrl);
                videoJobRepository.save(saved);
                publishEvent(saved, "COMPLETED");
            }

            @Override
            public void onError(String errorMessage) {
                saved.markFailed(errorMessage);
                videoJobRepository.save(saved);
                publishEvent(saved, "FAILED");
            }
        });

        return saved;
    }

    public VideoJob cancelJob(UUID id, UUID tenantId) {
        VideoJob job = getJob(id, tenantId);

        if (job.getStatus() == VideoJob.JobStatus.COMPLETED || job.getStatus() == VideoJob.JobStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel job in terminal state: " + job.getStatus());
        }

        job.setStatus(VideoJob.JobStatus.CANCELLED);
        job.setCompletedAt(LocalDateTime.now());
        VideoJob saved = videoJobRepository.save(job);

        publishEvent(saved, "CANCELLED");
        return saved;
    }

    private void publishEvent(VideoJob job, String eventType) {
        try {
            kafkaTemplate.send(TOPIC_VIDEO_EVENTS, job.getId().toString(), job);
            log.debug("Published event {} for job {}", eventType, job.getId());
        } catch (Exception e) {
            log.error("Failed to publish event {} for job {}: {}", eventType, job.getId(), e.getMessage());
        }
    }

    public static class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String message) {
            super(message);
        }
    }
}
