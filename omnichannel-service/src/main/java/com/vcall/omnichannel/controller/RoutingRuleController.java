package com.vcall.omnichannel.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.omnichannel.dto.request.RoutingRuleRequest;
import com.vcall.omnichannel.dto.response.RoutingRuleResponse;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.service.RoutingRuleService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/omnichannel/routing-rules")
@RequiredArgsConstructor
public class RoutingRuleController {

    private final RoutingRuleService routingRuleService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<RoutingRuleResponse>>> getAll(
            @RequestParam(required = false) Channel channel,
            Pageable pageable) {
        Page<RoutingRuleResponse> responses = routingRuleService.getAll(channel, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<RoutingRuleResponse>> getById(@PathVariable Long id) {
        RoutingRuleResponse response = routingRuleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<RoutingRuleResponse>> create(@Valid @RequestBody RoutingRuleRequest request) {
        RoutingRuleResponse response = routingRuleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Routing rule created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<RoutingRuleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RoutingRuleRequest request) {
        RoutingRuleResponse response = routingRuleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Routing rule updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        routingRuleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Routing rule deleted", null));
    }
}
