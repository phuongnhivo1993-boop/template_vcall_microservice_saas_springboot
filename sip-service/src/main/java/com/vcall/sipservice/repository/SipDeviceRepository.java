package com.vcall.sipservice.repository;

import com.vcall.sipservice.entity.SipDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SipDeviceRepository extends JpaRepository<SipDevice, Long>, JpaSpecificationExecutor<SipDevice> {
    List<SipDevice> findBySipAccountId(Long sipAccountId);
    Page<SipDevice> findBySipAccountId(Long sipAccountId, Pageable pageable);
    Optional<SipDevice> findByIpAddress(String ipAddress);
}
