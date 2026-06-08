package com.vcall.xr.video.controller;

import com.vcall.xr.video.domain.VideoJob;
import com.vcall.xr.video.domain.VideoType;
import com.vcall.xr.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@RestController
@RequestMapping("/api/v1/video-jobs")
@RequiredArgsConstructor
@Tag(name = "Video Jobs", description = "360 Video Processing Job Management")
public class VideoController {

    private final VideoService videoService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Create a new video processing job")
    public ResponseEntity<VideoJobResponse> createJob(
            @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-ID") UUID tenantId,
            @Valid @RequestBody CreateVideoJobRequest request) {
        log.info("Creating video job for tenant {} asset {}", tenantId, request.getAssetId());

        VideoJob job = videoService.createJob(
                tenantId,
                request.getAssetId(),
                request.getInputUrl(),
                request.getVideoType(),
                request.getTargetResolutions()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(VideoJobResponse.from(job));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get video job by ID")
    public ResponseEntity<VideoJobResponse> getJob(
            @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id) {
        VideoJob job = videoService.getJob(id, tenantId);
        return ResponseEntity.ok(VideoJobResponse.from(job));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "List all video jobs for tenant")
    public ResponseEntity<List<VideoJobResponse>> listJobs(
            @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-ID") UUID tenantId) {
        List<VideoJob> jobs = videoService.getJobsByTenant(tenantId);
        List<VideoJobResponse> responses = jobs.stream()
                .map(VideoJobResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Start processing a video job")
    public ResponseEntity<VideoJobResponse> processJob(
            @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id) {
        log.info("Processing video job {} for tenant {}", id, tenantId);
        VideoJob job = videoService.processJob(id, tenantId);
        return ResponseEntity.ok(VideoJobResponse.from(job));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Cancel a video job")
    public ResponseEntity<VideoJobResponse> cancelJob(
            @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id) {
        log.info("Cancelling video job {} for tenant {}", id, tenantId);
        VideoJob job = videoService.cancelJob(id, tenantId);
        return ResponseEntity.ok(VideoJobResponse.from(job));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get active job count for tenant")
    public ResponseEntity<JobStatsResponse> getStats(
            @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-ID") UUID tenantId) {
        long activeCount = videoService.getActiveJobCount(tenantId);
        return ResponseEntity.ok(new JobStatsResponse(activeCount));
    }

    @Data
    public static class CreateVideoJobRequest {
        @NotBlank(message = "Asset ID is required")
        private String assetId;

        @NotBlank(message = "Input URL is required")
        private String inputUrl;

        @NotNull(message = "Video type is required")
        private VideoType videoType;

        @NotNull(message = "Target resolutions are required")
        private List<String> targetResolutions;
    }

    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.Data
    public static class VideoJobResponse {
        private UUID id;
        private UUID tenantId;
        private String assetId;
        private String inputUrl;
        private String outputUrl;
        private VideoType videoType;
        private List<String> targetResolutions;
        private VideoJob.JobStatus status;
        private Integer progress;
        private String errorMessage;
        private String createdAt;
        private String completedAt;

        public static VideoJobResponse from(VideoJob job) {
            return VideoJobResponse.builder()
                    .id(job.getId())
                    .tenantId(job.getTenantId())
                    .assetId(job.getAssetId())
                    .inputUrl(job.getInputUrl())
                    .outputUrl(job.getOutputUrl())
                    .videoType(job.getVideoType())
                    .targetResolutions(job.getTargetResolutions())
                    .status(job.getStatus())
                    .progress(job.getProgress())
                    .errorMessage(job.getErrorMessage())
                    .createdAt(job.getCreatedAt() != null ? job.getCreatedAt().toString() : null)
                    .completedAt(job.getCompletedAt() != null ? job.getCompletedAt().toString() : null)
                    .build();
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class JobStatsResponse {
        private long activeJobs;
    }
}
