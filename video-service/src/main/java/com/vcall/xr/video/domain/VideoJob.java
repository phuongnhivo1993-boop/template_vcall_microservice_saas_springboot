package com.vcall.xr.video.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "xr_video_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {})
@EqualsAndHashCode(of = "id")
public class VideoJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, updatable = false)
    private String assetId;

    @Column(nullable = false, length = 1024)
    private String inputUrl;

    @Column(length = 1024)
    private String outputUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "video_type")
    private VideoType videoType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> targetResolutions;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "status")
    private JobStatus status;

    @Builder.Default
    @Column(nullable = false)
    private Integer progress = 0;

    @Column(length = 4096)
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = JobStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markProcessing() {
        this.status = JobStatus.PROCESSING;
        this.progress = 0;
    }

    public void markCompleted(String outputUrl) {
        this.status = JobStatus.COMPLETED;
        this.progress = 100;
        this.outputUrl = outputUrl;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    @Getter
    public enum JobStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }

    public interface ProgressCallback {
        void updateProgress(int progress);
        default void onCompleted(String outputUrl) {}
        default void onError(String errorMessage) {}
    }
}
