package com.vcall.cdr.controller;

import com.vcall.cdr.dto.CdrRecordResponse;
import com.vcall.cdr.dto.CdrSearchRequest;
import com.vcall.cdr.entity.CdrRecord;
import com.vcall.cdr.service.CdrService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cdr")
@RequiredArgsConstructor
public class CdrController {

    private final CdrService cdrService;

    @PostMapping
    public ResponseEntity<ApiResponse<CdrRecordResponse>> createCdr(@Valid @RequestBody CdrRecord record) {
        CdrRecordResponse response = cdrService.createCdr(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("CDR created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CdrRecordResponse>> getCdrById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cdrService.getById(id)));
    }

    @GetMapping("/call/{callId}")
    public ResponseEntity<ApiResponse<CdrRecordResponse>> getCdrByCallId(@PathVariable String callId) {
        return ResponseEntity.ok(ApiResponse.success(cdrService.getByCallId(callId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<CdrRecordResponse>>> searchCdr(@Valid CdrSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cdrService.searchCdr(request)));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<ApiResponse<List<CdrRecordResponse>>> getByAgentId(@PathVariable UUID agentId) {
        return ResponseEntity.ok(ApiResponse.success(cdrService.getByAgentId(agentId)));
    }
}
