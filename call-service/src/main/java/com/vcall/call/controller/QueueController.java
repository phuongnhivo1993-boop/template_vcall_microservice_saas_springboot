package com.vcall.call.controller;

import com.vcall.call.dto.QueueRequest;
import com.vcall.call.dto.QueueResponse;
import com.vcall.call.service.QueueService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/call-queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping
    public ResponseEntity<QueueResponse> createQueue(@Valid @RequestBody QueueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(queueService.createQueue(request));
    }

    @GetMapping
    public ResponseEntity<Page<QueueResponse>> getAllQueues(Pageable pageable) {
        return ResponseEntity.ok(queueService.getAllQueues(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueueResponse> getQueue(@PathVariable Long id) {
        return ResponseEntity.ok(queueService.getQueue(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QueueResponse> updateQueue(@PathVariable Long id, @Valid @RequestBody QueueRequest request) {
        return ResponseEntity.ok(queueService.updateQueue(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQueue(@PathVariable Long id) {
        queueService.deleteQueue(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{queueId}/agents/{agentId}")
    public ResponseEntity<Void> addAgentToQueue(@PathVariable Long queueId,
                                                 @PathVariable UUID agentId,
                                                 @RequestParam(required = false) Integer priority) {
        queueService.addAgentToQueue(queueId, agentId, priority);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{queueId}/agents/{agentId}")
    public ResponseEntity<Void> removeAgentFromQueue(@PathVariable Long queueId,
                                                      @PathVariable UUID agentId) {
        queueService.removeAgentFromQueue(queueId, agentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<QueueResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String strategy,
            Pageable pageable) {
        Page<QueueResponse> result = queueService.search(keyword, strategy, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<QueueResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = queueService.search(keyword, null, pageable);
        } else {
            items = queueService.getAllQueues(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Strategy", "Max Wait Time", "Max Queue Size", "Member Count");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "name", "strategy", "maxWaitTime", "maxQueueSize", "memberCount"));
        CsvExportUtil.writeCsv(response, "queues.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<QueueResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = queueService.search(keyword, null, pageable);
        } else {
            items = queueService.getAllQueues(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Strategy", "Max Wait Time", "Max Queue Size", "Member Count");
        ExcelExportUtil.writeExcel(response, "queues.xlsx", headers, items.getContent(),
                Arrays.asList("id", "name", "strategy", "maxWaitTime", "maxQueueSize", "memberCount"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(queueService.getStats()));
    }
}
