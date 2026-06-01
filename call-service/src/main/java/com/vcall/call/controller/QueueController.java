package com.vcall.call.controller;

import com.vcall.call.dto.QueueRequest;
import com.vcall.call.dto.QueueResponse;
import com.vcall.call.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
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
    public ResponseEntity<List<QueueResponse>> getAllQueues() {
        return ResponseEntity.ok(queueService.getAllQueues());
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
}
