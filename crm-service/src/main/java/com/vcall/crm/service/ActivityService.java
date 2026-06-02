package com.vcall.crm.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.crm.dto.ActivityRequest;
import com.vcall.crm.dto.ActivityResponse;
import com.vcall.crm.entity.Activity;
import com.vcall.crm.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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

    public Page<ActivityResponse> getAllActivities(Pageable pageable) {
        return activityRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<ActivityResponse> searchActivities(Specification<Activity> spec, Pageable pageable) {
        return activityRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public Map<String, Object> getActivityStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActivities", activityRepository.count());
        return stats;
    }

    public Page<ActivityResponse> getActivitiesByCustomer(UUID customerId, Pageable pageable) {
        return activityRepository.findByCustomerId(customerId, pageable).map(this::mapToResponse);
    }

    public Page<ActivityResponse> getActivitiesByLead(UUID leadId, Pageable pageable) {
        return activityRepository.findByLeadId(leadId, pageable).map(this::mapToResponse);
    }

    public Page<ActivityResponse> getActivitiesByDateRange(UUID assignedTo, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return activityRepository.findByAssignedToAndActivityDateBetween(assignedTo, start, end, pageable).map(this::mapToResponse);
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
        activity.setIsDeleted(true);
        activityRepository.save(activity);
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
