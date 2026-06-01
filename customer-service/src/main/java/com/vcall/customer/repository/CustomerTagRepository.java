package com.vcall.customer.repository;

import com.vcall.customer.entity.CustomerTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerTagRepository extends JpaRepository<CustomerTag, Long> {

    Optional<CustomerTag> findByName(String name);
}
