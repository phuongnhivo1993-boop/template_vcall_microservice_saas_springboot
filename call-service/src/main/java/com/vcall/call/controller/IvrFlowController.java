package com.vcall.call.controller;

import com.vcall.call.dto.IvrFlowRequest;
import com.vcall.call.dto.IvrFlowResponse;
import com.vcall.call.dto.IvrStepRequest;
import com.vcall.call.service.IvrFlowService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<List<IvrFlowResponse>> getAllFlows() {
        return ResponseEntity.ok(ivrFlowService.getAllFlows());
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
}
