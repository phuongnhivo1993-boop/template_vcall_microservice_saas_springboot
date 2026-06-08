package com.vcall.pbx.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.pbx.dto.PbxQueueRequest;
import com.vcall.pbx.dto.PbxQueueResponse;
import com.vcall.pbx.service.PbxQueueService;
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
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/pbx/queues")
@RequiredArgsConstructor
public class PbxQueueController {

    private final PbxQueueService pbxQueueService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<PbxQueueResponse>> createQueue(@Valid @RequestBody PbxQueueRequest request) {
        PbxQueueResponse response = pbxQueueService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Queue created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<PbxQueueResponse>>> getAllQueues(Pageable pageable) {
        Page<PbxQueueResponse> queues = pbxQueueService.getAllQueues(pageable);
        return ResponseEntity.ok(ApiResponse.success(queues));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<PbxQueueResponse>> getQueue(@PathVariable Long id) {
        PbxQueueResponse response = pbxQueueService.getQueue(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<PbxQueueResponse>> updateQueue(@PathVariable Long id,
                                                                       @Valid @RequestBody PbxQueueRequest request) {
        PbxQueueResponse response = pbxQueueService.updateQueue(id, request);
        return ResponseEntity.ok(ApiResponse.success("Queue updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteQueue(@PathVariable Long id) {
        pbxQueueService.deleteQueue(id);
        return ResponseEntity.ok(ApiResponse.success("Queue deleted successfully", null));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long extensionId = Long.valueOf(body.get("extensionId").toString());
        Integer priority = body.get("priority") != null ? Integer.valueOf(body.get("priority").toString()) : null;
        pbxQueueService.addMember(id, extensionId, priority);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", null));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        pbxQueueService.removeMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<PbxQueueResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String strategy,
            Pageable pageable) {
        Page<PbxQueueResponse> result = pbxQueueService.search(keyword, strategy, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<PbxQueueResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = pbxQueueService.search(keyword, null, pageable);
        } else {
            items = pbxQueueService.getAllQueues(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description", "Strategy", "Max Wait Time", "Max Queue Size", "Timeout Action", "Timeout Dest", "Member Count");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "name", "description", "strategy", "maxWaitTime", "maxQueueSize", "timeoutAction", "timeoutDestination", "memberCount"));
        CsvExportUtil.writeCsv(response, "pbx-queues.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<PbxQueueResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = pbxQueueService.search(keyword, null, pageable);
        } else {
            items = pbxQueueService.getAllQueues(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description", "Strategy", "Max Wait Time", "Max Queue Size", "Timeout Action", "Timeout Dest", "Member Count");
        ExcelExportUtil.writeExcel(response, "pbx-queues.xlsx", headers, items.getContent(),
                Arrays.asList("id", "name", "description", "strategy", "maxWaitTime", "maxQueueSize", "timeoutAction", "timeoutDestination", "memberCount"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(pbxQueueService.getStats()));
    }
}
