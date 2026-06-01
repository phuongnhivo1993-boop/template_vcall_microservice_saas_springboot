package com.vcall.email.repository;

import com.vcall.email.entity.EmailAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailAccountRepository extends JpaRepository<EmailAccount, Long> {

    Optional<EmailAccount> findByEmailAddress(String emailAddress);

    Optional<EmailAccount> findByIsDefaultTrue();
}
