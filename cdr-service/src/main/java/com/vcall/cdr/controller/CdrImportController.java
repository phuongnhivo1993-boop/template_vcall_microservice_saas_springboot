package com.vcall.cdr.controller;

import com.vcall.cdr.dto.CdrImportRequest;
import com.vcall.cdr.entity.CdrImportLog;
import com.vcall.cdr.service.CdrImportService;
import com.vcall.common.dto.ApiResponse;
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

@RestController
@RequestMapping("/api/v1/cdr/import")
@RequiredArgsConstructor
public class CdrImportController {

    private final CdrImportService cdrImportService;

    @PostMapping
    public ResponseEntity<ApiResponse<CdrImportLog>> importCdr(@Valid @RequestBody CdrImportRequest request) {
        CdrImportLog importLog = cdrImportService.importFromFile(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success("Import started", importLog));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CdrImportLog>> getImportStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(cdrImportService.getImportStatus(id)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<CdrImportLog>>> getImportHistory() {
        return ResponseEntity.ok(ApiResponse.success(cdrImportService.getImportHistory()));
    }
}
