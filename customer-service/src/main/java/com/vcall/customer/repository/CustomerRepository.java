package com.vcall.customer.repository;

import com.vcall.common.repository.TenantAwareRepository;
import com.vcall.customer.entity.Customer;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends TenantAwareRepository<Customer, UUID> {

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
