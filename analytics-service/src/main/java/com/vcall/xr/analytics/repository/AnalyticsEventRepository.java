package com.vcall.xr.analytics.repository;

import com.vcall.xr.analytics.domain.AnalyticsEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {

    Page<AnalyticsEvent> findBySessionId(UUID sessionId, Pageable pageable);

    Page<AnalyticsEvent> findByUserId(UUID userId, Pageable pageable);

    Page<AnalyticsEvent> findBySceneId(UUID sceneId, Pageable pageable);

    Page<AnalyticsEvent> findByEventType(String eventType, Pageable pageable);

    Page<AnalyticsEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.tenantId = :tenantId AND e.timestamp BETWEEN :start AND :end")
    long countByTenantIdAndDateRange(@Param("tenantId") String tenantId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("SELECT e.eventType, COUNT(e) FROM AnalyticsEvent e WHERE e.tenantId = :tenantId AND e.timestamp BETWEEN :start AND :end GROUP BY e.eventType")
    List<Object[]> countByEventTypeGrouped(@Param("tenantId") String tenantId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT e.deviceType, COUNT(e) FROM AnalyticsEvent e WHERE e.tenantId = :tenantId AND e.timestamp BETWEEN :start AND :end GROUP BY e.deviceType")
    List<Object[]> countByDeviceTypeGrouped(@Param("tenantId") String tenantId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("SELECT e.sceneId, COUNT(e) FROM AnalyticsEvent e WHERE e.tenantId = :tenantId AND e.eventType = :eventType AND e.timestamp BETWEEN :start AND :end GROUP BY e.sceneId ORDER BY COUNT(e) DESC")
    List<Object[]> topScenesByEventType(@Param("tenantId") String tenantId,
                                        @Param("eventType") String eventType,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        Pageable pageable);

    @Query("SELECT e.sessionId, COUNT(e) FROM AnalyticsEvent e WHERE e.tenantId = :tenantId AND e.timestamp BETWEEN :start AND :end GROUP BY e.sessionId")
    List<Object[]> sessionActivityCounts(@Param("tenantId") String tenantId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}
