package com.vcall.customer360.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.PagedResponse;
import com.vcall.customer360.dto.Customer360Response;
import com.vcall.customer360.service.Customer360Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer360")
@RequiredArgsConstructor
public class Customer360Controller {

    private final Customer360Service customer360Service;

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Customer360Response>> getCustomer360(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(customer360Service.getCustomer360(customerId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<Customer360Response>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String segment,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastContactAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<Customer360Response> result = customer360Service.search(keyword, segment, sortBy, sortDir, page, size);
        PagedResponse<Customer360Response> paged = PagedResponse.<Customer360Response>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Customer360Response>> updateProfile(
            @PathVariable UUID customerId,
            @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(ApiResponse.success(customer360Service.updateProfile(customerId, updates)));
    }
}
