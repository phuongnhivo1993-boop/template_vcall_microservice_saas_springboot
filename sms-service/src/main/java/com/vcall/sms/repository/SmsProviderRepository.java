package com.vcall.sms.repository;

import com.vcall.sms.entity.SmsProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmsProviderRepository extends JpaRepository<SmsProvider, Long> {

    List<SmsProvider> findByProviderType(SmsProvider.ProviderType providerType);

    Optional<SmsProvider> findByIsDefaultTrue();

    List<SmsProvider> findByIsActiveTrue();
}
