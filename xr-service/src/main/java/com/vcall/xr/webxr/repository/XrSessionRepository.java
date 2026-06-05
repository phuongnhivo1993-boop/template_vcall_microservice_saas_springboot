package com.vcall.xr.webxr.repository;

import com.vcall.xr.webxr.domain.DeviceType;
import com.vcall.xr.webxr.domain.XrSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface XrSessionRepository extends JpaRepository<XrSession, UUID> {

    Page<XrSession> findByUserId(UUID userId, Pageable pageable);

    Page<XrSession> findBySceneId(UUID sceneId, Pageable pageable);

    Page<XrSession> findByDeviceType(DeviceType deviceType, Pageable pageable);

    Page<XrSession> findByStartedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT COUNT(x) FROM XrSession x WHERE x.endedAt IS NULL")
    long countActiveSessions();

    @Query("SELECT COUNT(x) FROM XrSession x WHERE x.tenantId = :tenantId AND x.startedAt BETWEEN :start AND :end")
    long countByTenantIdAndDateRange(@Param("tenantId") String tenantId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("SELECT AVG(x.fpsAvg) FROM XrSession x WHERE x.fpsAvg IS NOT NULL AND x.tenantId = :tenantId")
    Double averageFpsByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT AVG(x.loadTimeMs) FROM XrSession x WHERE x.loadTimeMs IS NOT NULL AND x.tenantId = :tenantId")
    Double averageLoadTimeByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT x.deviceType, COUNT(x) FROM XrSession x WHERE x.tenantId = :tenantId GROUP BY x.deviceType")
    java.util.List<Object[]> countByDeviceTypeGrouped(@Param("tenantId") String tenantId);
}
