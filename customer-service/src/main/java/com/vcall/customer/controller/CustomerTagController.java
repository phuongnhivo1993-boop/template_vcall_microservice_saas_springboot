package com.vcall.customer.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.customer.dto.CustomerTagRequest;
import com.vcall.customer.dto.CustomerTagResponse;
import com.vcall.customer.service.CustomerTagService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/customer-tags")
@RequiredArgsConstructor
public class CustomerTagController {

    private final CustomerTagService customerTagService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<CustomerTagResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerTagService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<CustomerTagResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerTagService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<CustomerTagResponse>> create(@Valid @RequestBody CustomerTagRequest request) {
        CustomerTagResponse response = customerTagService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tag created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<CustomerTagResponse>> update(@PathVariable Long id,
                                                                    @Valid @RequestBody CustomerTagRequest request) {
        CustomerTagResponse response = customerTagService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tag updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerTagService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Tag deleted", null));
    }
}
