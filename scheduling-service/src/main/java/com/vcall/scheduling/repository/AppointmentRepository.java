package com.vcall.scheduling.repository;

import com.vcall.scheduling.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByCustomerId(UUID customerId);

    List<Appointment> findByAgentId(UUID agentId);

    List<Appointment> findByAgentIdAndStartTimeBetween(UUID agentId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    long countByStatus(Appointment.AppointmentStatus status);
}
