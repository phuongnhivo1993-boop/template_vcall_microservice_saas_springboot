package com.vcall.call.controller;

import com.vcall.call.dto.CallRequest;
import com.vcall.call.dto.CallResponse;
import com.vcall.call.dto.CallStatusRequest;
import com.vcall.call.entity.Call;
import com.vcall.call.service.CallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    @PostMapping
    public ResponseEntity<CallResponse> initiateCall(@Valid @RequestBody CallRequest request) {
        CallResponse response = callService.createCall(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CallResponse> getCall(@PathVariable UUID id) {
        return ResponseEntity.ok(callService.getCall(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CallResponse> updateCallStatus(@PathVariable UUID id,
                                                          @Valid @RequestBody CallStatusRequest request) {
        return ResponseEntity.ok(callService.updateCallStatus(id, request));
    }

    @PostMapping("/{id}/hangup")
    public ResponseEntity<CallResponse> hangupCall(@PathVariable UUID id) {
        return ResponseEntity.ok(callService.hangupCall(id));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<CallResponse>> getAgentActiveCalls(@PathVariable UUID agentId) {
        return ResponseEntity.ok(callService.getAgentActiveCalls(agentId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CallResponse>> getActiveCalls(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusHours(24);
        if (end == null) end = LocalDateTime.now();
        return ResponseEntity.ok(callService.getCallsByDateRange(Call.CallStatus.IN_PROGRESS, start, end));
    }
}
