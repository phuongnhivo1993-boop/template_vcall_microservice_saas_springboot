package com.vcall.scheduling.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.scheduling.dto.request.AppointmentRequest;
import com.vcall.scheduling.dto.response.AppointmentResponse;
import com.vcall.scheduling.entity.Appointment;
import com.vcall.scheduling.entity.Appointment.AppointmentStatus;
import com.vcall.scheduling.entity.Appointment.AppointmentType;
import com.vcall.scheduling.mapper.SchedulingMapper;
import com.vcall.scheduling.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SchedulingMapper mapper;

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        Appointment appointment = mapper.toEntity(request);
        appointment = appointmentRepository.save(appointment);
        return mapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return mapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> searchAppointments(Specification<Appointment> spec, Pageable pageable) {
        return appointmentRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional
    public AppointmentResponse updateAppointment(UUID id, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        mapper.updateEntity(request, appointment);
        appointment = appointmentRepository.save(appointment);
        return mapper.toResponse(appointment);
    }

    @Transactional
    public void deleteAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        appointment.setIsDeleted(true);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public AppointmentResponse updateStatus(UUID id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        AppointmentStatus newStatus = AppointmentStatus.valueOf(status.toUpperCase());
        appointment.setStatus(newStatus);
        appointment = appointmentRepository.save(appointment);
        return mapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByAgentAndDateRange(UUID agentId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return appointmentRepository.findByAgentIdAndStartTimeBetween(agentId, start, end)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByCustomer(UUID customerId) {
        return appointmentRepository.findByCustomerId(customerId)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByAgent(UUID agentId) {
        return appointmentRepository.findByAgentId(agentId)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getStats() {
        List<Appointment> all = appointmentRepository.findAll();
        return all.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));
    }
}
