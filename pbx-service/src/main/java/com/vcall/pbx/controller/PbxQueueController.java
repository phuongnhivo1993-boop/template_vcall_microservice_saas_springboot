package com.vcall.pbx.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.pbx.dto.PbxQueueRequest;
import com.vcall.pbx.dto.PbxQueueResponse;
import com.vcall.pbx.service.PbxQueueService;
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
@RequestMapping("/api/v1/pbx/queues")
@RequiredArgsConstructor
public class PbxQueueController {

    private final PbxQueueService pbxQueueService;

    @PostMapping
    public ResponseEntity<ApiResponse<PbxQueueResponse>> createQueue(@Valid @RequestBody PbxQueueRequest request) {
        PbxQueueResponse response = pbxQueueService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Queue created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PbxQueueResponse>>> getAllQueues(Pageable pageable) {
        Page<PbxQueueResponse> queues = pbxQueueService.getAllQueues(pageable);
        return ResponseEntity.ok(ApiResponse.success(queues));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PbxQueueResponse>> getQueue(@PathVariable Long id) {
        PbxQueueResponse response = pbxQueueService.getQueue(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PbxQueueResponse>> updateQueue(@PathVariable Long id,
                                                                       @Valid @RequestBody PbxQueueRequest request) {
        PbxQueueResponse response = pbxQueueService.updateQueue(id, request);
        return ResponseEntity.ok(ApiResponse.success("Queue updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQueue(@PathVariable Long id) {
        pbxQueueService.deleteQueue(id);
        return ResponseEntity.ok(ApiResponse.success("Queue deleted successfully", null));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long extensionId = Long.valueOf(body.get("extensionId").toString());
        Integer priority = body.get("priority") != null ? Integer.valueOf(body.get("priority").toString()) : null;
        pbxQueueService.addMember(id, extensionId, priority);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", null));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        pbxQueueService.removeMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}
