package com.vcall.call.controller;

import com.vcall.call.dto.IvrFlowRequest;
import com.vcall.call.dto.IvrFlowResponse;
import com.vcall.call.dto.IvrStepRequest;
import com.vcall.call.service.IvrFlowService;
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

@RestController
@RequestMapping("/api/v1/ivr-flows")
@RequiredArgsConstructor
public class IvrFlowController {

    private final IvrFlowService ivrFlowService;

    @PostMapping
    public ResponseEntity<IvrFlowResponse> createFlow(@Valid @RequestBody IvrFlowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ivrFlowService.createFlow(request));
    }

    @GetMapping
    public ResponseEntity<Page<IvrFlowResponse>> getAllFlows(Pageable pageable) {
        return ResponseEntity.ok(ivrFlowService.getAllFlows(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IvrFlowResponse> getFlow(@PathVariable Long id) {
        return ResponseEntity.ok(ivrFlowService.getFlow(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IvrFlowResponse> updateFlow(@PathVariable Long id, @Valid @RequestBody IvrFlowRequest request) {
        return ResponseEntity.ok(ivrFlowService.updateFlow(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlow(@PathVariable Long id) {
        ivrFlowService.deleteFlow(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{flowId}/steps")
    public ResponseEntity<IvrStepRequest> addStep(@PathVariable Long flowId,
                                                    @Valid @RequestBody IvrStepRequest stepRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ivrFlowService.addStep(flowId, stepRequest));
    }

    @GetMapping("/{flowId}/steps")
    public ResponseEntity<List<IvrStepRequest>> getSteps(@PathVariable Long flowId) {
        return ResponseEntity.ok(ivrFlowService.getSteps(flowId));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<IvrFlowResponse>>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<IvrFlowResponse> result = ivrFlowService.search(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam(required = false) String keyword,
                           HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<IvrFlowResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = ivrFlowService.search(keyword, pageable);
        } else {
            items = ivrFlowService.getAllFlows(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description", "Greeting Message", "Fallback", "Timeout");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "name", "description", "greetingMessage", "fallbackDestination", "timeout"));
        CsvExportUtil.writeCsv(response, "ivr-flows.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<IvrFlowResponse> items;
        if (keyword != null && !keyword.isEmpty()) {
            items = ivrFlowService.search(keyword, pageable);
        } else {
            items = ivrFlowService.getAllFlows(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description", "Greeting Message", "Fallback", "Timeout");
        ExcelExportUtil.writeExcel(response, "ivr-flows.xlsx", headers, items.getContent(),
                Arrays.asList("id", "name", "description", "greetingMessage", "fallbackDestination", "timeout"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(ivrFlowService.getStats()));
    }
}
