package com.vcall.xr.video.repository;

import com.vcall.xr.video.domain.VideoJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoJobRepository extends JpaRepository<VideoJob, UUID> {

    List<VideoJob> findByTenantId(UUID tenantId);

    Page<VideoJob> findByTenantId(UUID tenantId, Pageable pageable);

    List<VideoJob> findByTenantIdAndStatus(UUID tenantId, VideoJob.JobStatus status);

    Optional<VideoJob> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT vj FROM VideoJob vj WHERE vj.status = :status ORDER BY vj.createdAt ASC")
    List<VideoJob> findByStatusOrderedByCreatedAt(@Param("status") VideoJob.JobStatus status);

    @Query("SELECT COUNT(vj) FROM VideoJob vj WHERE vj.tenantId = :tenantId AND vj.status = 'PROCESSING'")
    long countActiveProcessingJobs(@Param("tenantId") UUID tenantId);

    @Query("SELECT vj FROM VideoJob vj WHERE vj.assetId = :assetId")
    Optional<VideoJob> findByAssetId(@Param("assetId") String assetId);

    boolean existsByAssetIdAndStatus(String assetId, VideoJob.JobStatus status);
}
