package com.vcall.customer.repository;

import com.vcall.common.repository.TenantAwareRepository;
import com.vcall.customer.entity.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends TenantAwareRepository<Customer, UUID> {

    @EntityGraph(attributePaths = {"contacts", "addresses", "tagMappings"})
    List<Customer> findAll();

    @EntityGraph(attributePaths = {"contacts", "addresses", "tagMappings"})
    Optional<Customer> findByCustomerCode(String customerCode);

    @EntityGraph(attributePaths = {"contacts", "addresses", "tagMappings"})
    Optional<Customer> findByEmail(String email);

    @EntityGraph(attributePaths = {"contacts", "addresses", "tagMappings"})
    Optional<Customer> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
