package com.vcall.crm.repository;

import com.vcall.crm.entity.Lead;
import com.vcall.crm.entity.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findByEmail(String email);

    Optional<Lead> findByPhone(String phone);

    List<Lead> findByStatus(LeadStatus status);

    List<Lead> findByAssignedTo(UUID assignedTo);

    long countByStatus(LeadStatus status);

    @Query(value = "SELECT * FROM leads WHERE id = ?1", nativeQuery = true)
    java.util.Optional<Lead> findByIdIncludingDeleted(java.util.UUID id);
}
