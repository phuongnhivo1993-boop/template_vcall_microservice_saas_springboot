package com.vcall.scheduling.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.scheduling.dto.response.AvailabilityResponse;
import com.vcall.scheduling.entity.AgentAvailability;
import com.vcall.scheduling.entity.AgentAvailability.AvailabilityStatus;
import com.vcall.scheduling.mapper.SchedulingMapper;
import com.vcall.scheduling.repository.AgentAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AgentAvailabilityRepository availabilityRepository;
    private final SchedulingMapper mapper;

    @Transactional
    public AvailabilityResponse createAvailability(AgentAvailability availability) {
        availability = availabilityRepository.save(availability);
        return mapper.toResponse(availability);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(UUID id) {
        AgentAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));
        return mapper.toResponse(availability);
    }

    @Transactional(readOnly = true)
    public Page<AvailabilityResponse> getAllAvailabilities(Pageable pageable) {
        return availabilityRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AvailabilityResponse> searchAvailabilities(Specification<AgentAvailability> spec, Pageable pageable) {
        return availabilityRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional
    public void deleteAvailability(UUID id) {
        AgentAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));
        availability.setIsDeleted(true);
        availabilityRepository.save(availability);
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(UUID agentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<AgentAvailability> slots = availabilityRepository
                .findByAgentIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(agentId, date, endTime, startTime);
        return slots.isEmpty() || slots.stream().noneMatch(s -> Boolean.TRUE.equals(s.getIsBooked()));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getByAgentAndDate(UUID agentId, LocalDate date) {
        return availabilityRepository.findByAgentIdAndDate(agentId, date)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getByAgentAndDateRange(UUID agentId, LocalDate start, LocalDate end) {
        return availabilityRepository.findByAgentIdAndDateBetween(agentId, start, end)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public AvailabilityResponse updateStatus(UUID id, String status) {
        AgentAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));
        availability.setStatus(AvailabilityStatus.valueOf(status.toUpperCase()));
        availability = availabilityRepository.save(availability);
        return mapper.toResponse(availability);
    }

    @Transactional
    public AvailabilityResponse toggleBooked(UUID id) {
        AgentAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));
        availability.setIsBooked(!Boolean.TRUE.equals(availability.getIsBooked()));
        availability = availabilityRepository.save(availability);
        return mapper.toResponse(availability);
    }
}
