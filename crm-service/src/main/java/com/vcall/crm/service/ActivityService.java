package com.vcall.crm.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.crm.dto.ActivityRequest;
import com.vcall.crm.dto.ActivityResponse;
import com.vcall.crm.entity.Activity;
import com.vcall.crm.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Transactional
    public ActivityResponse createActivity(ActivityRequest request) {
        Activity activity = new Activity();
        mapToEntity(request, activity);
        activity = activityRepository.save(activity);
        return mapToResponse(activity);
    }

    public ActivityResponse getActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
        return mapToResponse(activity);
    }

    public List<ActivityResponse> getAllActivities() {
        return activityRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ActivityResponse> getActivitiesByCustomer(UUID customerId) {
        return activityRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ActivityResponse> getActivitiesByLead(UUID leadId) {
        return activityRepository.findByLeadId(leadId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ActivityResponse> getActivitiesByDateRange(UUID assignedTo, LocalDateTime start, LocalDateTime end) {
        return activityRepository.findByAssignedToAndActivityDateBetween(assignedTo, start, end).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ActivityResponse updateActivity(Long id, ActivityRequest request) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
        mapToEntity(request, activity);
        activity = activityRepository.save(activity);
        return mapToResponse(activity);
    }

    @Transactional
    public void deleteActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
        activityRepository.delete(activity);
    }

    private void mapToEntity(ActivityRequest request, Activity activity) {
        activity.setCustomerId(request.getCustomerId());
        activity.setLeadId(request.getLeadId());
        activity.setType(request.getType());
        activity.setSubject(request.getSubject());
        activity.setDescription(request.getDescription());
        activity.setActivityDate(request.getActivityDate());
        activity.setDuration(request.getDuration());
        activity.setAssignedTo(request.getAssignedTo());
        activity.setResult(request.getResult());
    }

    private ActivityResponse mapToResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .customerId(activity.getCustomerId())
                .leadId(activity.getLeadId())
                .type(activity.getType())
                .subject(activity.getSubject())
                .description(activity.getDescription())
                .activityDate(activity.getActivityDate())
                .duration(activity.getDuration())
                .assignedTo(activity.getAssignedTo())
                .result(activity.getResult())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
