package com.vcall.pbx.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.pbx.dto.RingGroupRequest;
import com.vcall.pbx.dto.RingGroupResponse;
import com.vcall.pbx.service.RingGroupService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.Arrays;
import java.util.List;
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<RingGroupResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String strategy,
            Pageable pageable) {
        Page<RingGroupResponse> result = ringGroupService.search(keyword, strategy, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<RingGroupResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = ringGroupService.search(keyword, null, pageable);
        } else {
            items = ringGroupService.getAllRingGroups(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description", "Strategy", "Ring Timeout", "Member Count");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "name", "description", "strategy", "ringTimeout", "memberCount"));
        CsvExportUtil.writeCsv(response, "ring-groups.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<RingGroupResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = ringGroupService.search(keyword, null, pageable);
        } else {
            items = ringGroupService.getAllRingGroups(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description", "Strategy", "Ring Timeout", "Member Count");
        ExcelExportUtil.writeExcel(response, "ring-groups.xlsx", headers, items.getContent(),
                Arrays.asList("id", "name", "description", "strategy", "ringTimeout", "memberCount"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(ringGroupService.getStats()));
    }
}
