package com.vcall.crm.repository;

import com.vcall.crm.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByCustomerId(UUID customerId);

    List<Activity> findByLeadId(UUID leadId);

    List<Activity> findByAssignedToAndActivityDateBetween(UUID assignedTo, LocalDateTime start, LocalDateTime end);
}
