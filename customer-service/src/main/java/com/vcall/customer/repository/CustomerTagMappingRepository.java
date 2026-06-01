package com.vcall.customer.repository;

import com.vcall.customer.entity.CustomerTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerTagMappingRepository extends JpaRepository<CustomerTagMapping, Long> {

    List<CustomerTagMapping> findByCustomerId(UUID customerId);
}
