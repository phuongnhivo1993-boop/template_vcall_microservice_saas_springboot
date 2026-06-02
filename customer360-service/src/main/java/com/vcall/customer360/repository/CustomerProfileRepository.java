package com.vcall.customer360.repository;

import com.vcall.customer360.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID>, JpaSpecificationExecutor<CustomerProfile> {
    Optional<CustomerProfile> findByCustomerId(UUID customerId);
    Optional<CustomerProfile> findByPhone(String phone);
    Optional<CustomerProfile> findByEmail(String email);
}
