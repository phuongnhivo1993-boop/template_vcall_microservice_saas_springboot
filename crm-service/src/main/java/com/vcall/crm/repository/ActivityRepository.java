package com.vcall.crm.repository;

import com.vcall.crm.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    List<Activity> findByCustomerId(UUID customerId);

    Page<Activity> findByCustomerId(UUID customerId, Pageable pageable);

    List<Activity> findByLeadId(UUID leadId);

    Page<Activity> findByLeadId(UUID leadId, Pageable pageable);

    List<Activity> findByAssignedToAndActivityDateBetween(UUID assignedTo, LocalDateTime start, LocalDateTime end);

    Page<Activity> findByAssignedToAndActivityDateBetween(UUID assignedTo, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
