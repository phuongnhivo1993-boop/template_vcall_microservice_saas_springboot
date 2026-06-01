package com.vcall.sipservice.repository;

import com.vcall.sipservice.entity.SipAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SipAccountRepository extends JpaRepository<SipAccount, Long> {
    Optional<SipAccount> findByUsername(String username);
    List<SipAccount> findByTenantId(UUID tenantId);
    List<SipAccount> findByStatus(SipAccount.AccountStatus status);
    boolean existsByUsername(String username);
}
