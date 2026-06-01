package com.vcall.sipservice.repository;

import com.vcall.sipservice.entity.SipDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SipDeviceRepository extends JpaRepository<SipDevice, Long> {
    List<SipDevice> findBySipAccountId(Long sipAccountId);
    Optional<SipDevice> findByIpAddress(String ipAddress);
}
