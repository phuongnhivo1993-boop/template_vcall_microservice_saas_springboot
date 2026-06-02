package com.vcall.email.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.email.dto.EmailTemplateRequest;
import com.vcall.email.dto.EmailTemplateResponse;
import com.vcall.email.service.EmailTemplateService;
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

@RestController
@RequestMapping("/api/v1/email/templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @PostMapping
    public ResponseEntity<EmailTemplateResponse> createTemplate(@Valid @RequestBody EmailTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailTemplateService.createTemplate(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplateResponse> updateTemplate(@PathVariable Long id,
                                                                 @Valid @RequestBody EmailTemplateRequest request) {
        return ResponseEntity.ok(emailTemplateService.updateTemplate(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplateResponse> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(emailTemplateService.getTemplate(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmailTemplateResponse>>> getAllTemplates(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(emailTemplateService.getAllTemplates(pageable)));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<Page<EmailTemplateResponse>>> getByCategory(@RequestParam String category,
                                                                                   Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(emailTemplateService.getByCategory(category, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        emailTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
