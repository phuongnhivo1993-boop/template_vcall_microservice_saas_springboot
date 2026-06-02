package com.vcall.pbx.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.pbx.dto.RingGroupRequest;
import com.vcall.pbx.dto.RingGroupResponse;
import com.vcall.pbx.service.RingGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pbx/ring-groups")
@RequiredArgsConstructor
public class RingGroupController {

    private final RingGroupService ringGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<RingGroupResponse>> createRingGroup(@Valid @RequestBody RingGroupRequest request) {
        RingGroupResponse response = ringGroupService.createRingGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ring group created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RingGroupResponse>>> getAllRingGroups(Pageable pageable) {
        Page<RingGroupResponse> ringGroups = ringGroupService.getAllRingGroups(pageable);
        return ResponseEntity.ok(ApiResponse.success(ringGroups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RingGroupResponse>> getRingGroup(@PathVariable Long id) {
        RingGroupResponse response = ringGroupService.getRingGroup(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RingGroupResponse>> updateRingGroup(@PathVariable Long id,
                                                                            @Valid @RequestBody RingGroupRequest request) {
        RingGroupResponse response = ringGroupService.updateRingGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success("Ring group updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRingGroup(@PathVariable Long id) {
        ringGroupService.deleteRingGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Ring group deleted successfully", null));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long extensionId = Long.valueOf(body.get("extensionId").toString());
        Integer position = body.get("position") != null ? Integer.valueOf(body.get("position").toString()) : null;
        ringGroupService.addMember(id, extensionId, position);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", null));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        ringGroupService.removeMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}
