package com.vcall.sipservice.repository;

import com.vcall.sipservice.entity.SipRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SipRegistrationRepository extends JpaRepository<SipRegistration, Long> {
    List<SipRegistration> findBySipAccountId(Long sipAccountId);
    List<SipRegistration> findByStatus(SipRegistration.RegistrationStatus status);
    List<SipRegistration> findByExpiresBefore(LocalDateTime dateTime);
}
