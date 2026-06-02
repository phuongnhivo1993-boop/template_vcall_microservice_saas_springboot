package com.vcall.crm.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.crm.dto.ActivityRequest;
import com.vcall.crm.dto.ActivityResponse;
import com.vcall.crm.service.ActivityService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(@Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.createActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Activity created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(@PathVariable Long id) {
        ActivityResponse response = activityService.getActivity(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getAllActivities(
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        Page<ActivityResponse> responses;
        if (assignedTo != null && startDate != null && endDate != null) {
            responses = activityService.getActivitiesByDateRange(assignedTo, startDate, endDate, pageable);
        } else {
            responses = activityService.getAllActivities(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getActivitiesByCustomer(
            @PathVariable UUID customerId, Pageable pageable) {
        Page<ActivityResponse> responses = activityService.getActivitiesByCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.updateActivity(id, request);
        return ResponseEntity.ok(ApiResponse.success("Activity updated successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> searchActivities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        Specification<com.vcall.crm.entity.Activity> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("subject")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (assignedTo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignedTo"), assignedTo));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("activityDate"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("activityDate"), endDate));
        }
        Page<ActivityResponse> response = activityService.searchActivities(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    public void exportActivitiesCsv(@RequestParam(required = false) String keyword,
                                    HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("activityDate").descending());
        Specification<com.vcall.crm.entity.Activity> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("subject")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        Page<ActivityResponse> activities = activityService.searchActivities(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Subject", "Type", "Description", "Activity Date", "Duration", "Assigned To", "Result");
        List<List<String>> rows = CsvExportUtil.toRows(activities.getContent(),
                Arrays.asList("id", "subject", "type", "description", "activityDate", "duration", "assignedTo", "result"));
        CsvExportUtil.writeCsv(response, "activities.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportActivitiesExcel(@RequestParam(required = false) String keyword,
                                      HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("activityDate").descending());
        Specification<com.vcall.crm.entity.Activity> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("subject")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        Page<ActivityResponse> activities = activityService.searchActivities(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Subject", "Type", "Description", "Activity Date", "Duration", "Assigned To", "Result");
        ExcelExportUtil.writeExcel(response, "activities.xlsx", headers, activities.getContent(),
                Arrays.asList("id", "subject", "type", "description", "activityDate", "duration", "assignedTo", "result"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = activityService.getActivityStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.ok(ApiResponse.success("Activity deleted successfully", null));
    }
}
