package com.vcall.notification.repository;

import com.vcall.notification.entity.PushDevice;
import com.vcall.notification.entity.PushPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {

    List<PushDevice> findByUserId(UUID userId);

    Optional<PushDevice> findByDeviceToken(String deviceToken);

    List<PushDevice> findByPlatformAndIsActiveTrue(PushPlatform platform);

    Page<PushDevice> findByUserIdAndIsActiveTrue(UUID userId, Pageable pageable);
}
