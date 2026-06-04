package com.vcall.crm.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.CsvUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.common.util.ExcelImportUtil;
import com.vcall.crm.dto.CustomerNoteRequest;
import com.vcall.crm.dto.CustomerNoteResponse;
import com.vcall.crm.service.CustomerNoteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/notes")
@RequiredArgsConstructor
public class CustomerNoteController {

    private final CustomerNoteService customerNoteService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerNoteResponse>> createNote(@Valid @RequestBody CustomerNoteRequest request) {
        CustomerNoteResponse response = customerNoteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Note created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerNoteResponse>> getNote(@PathVariable Long id) {
        CustomerNoteResponse response = customerNoteService.getNote(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerNoteResponse>>> getAllNotes(Pageable pageable) {
        Page<CustomerNoteResponse> responses = customerNoteService.getAllNotes(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<CustomerNoteResponse>>> getNotesByCustomer(
            @PathVariable UUID customerId, Pageable pageable) {
        Page<CustomerNoteResponse> responses = customerNoteService.getNotesByCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerNoteResponse>> updateNote(@PathVariable Long id, @Valid @RequestBody CustomerNoteRequest request) {
        CustomerNoteResponse response = customerNoteService.updateNote(id, request);
        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CustomerNoteResponse>>> searchNotes(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Specification<com.vcall.crm.entity.CustomerNote> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("content")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        Page<CustomerNoteResponse> response = customerNoteService.searchNotes(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    public void exportNotesCsv(@RequestParam(required = false) String keyword,
                               HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<com.vcall.crm.entity.CustomerNote> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("content")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        Page<CustomerNoteResponse> notes = customerNoteService.searchNotes(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Title", "Content", "Pinned");
        List<List<String>> rows = CsvExportUtil.toRows(notes.getContent(),
                Arrays.asList("id", "title", "content", "pinned"));
        CsvExportUtil.writeCsv(response, "customer-notes.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportNotesExcel(@RequestParam(required = false) String keyword,
                                 HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<com.vcall.crm.entity.CustomerNote> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("content")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        Page<CustomerNoteResponse> notes = customerNoteService.searchNotes(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Title", "Content", "Pinned");
        ExcelExportUtil.writeExcel(response, "customer-notes.xlsx", headers, notes.getContent(),
                Arrays.asList("id", "title", "content", "pinned"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = customerNoteService.getNoteStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/import/csv")
    public ResponseEntity<ApiResponse<List<CustomerNoteResponse>>> importNotesCsv(
            @RequestParam("file") MultipartFile file) throws Exception {
        List<String[]> rows = CsvUtil.parseCsv(file.getInputStream());
        List<CustomerNoteResponse> importedNotes = new ArrayList<>();
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length >= 4) {
                CustomerNoteRequest request = new CustomerNoteRequest();
                request.setTitle(row[0]);
                request.setContent(row[1]);
                request.setPinned(Boolean.parseBoolean(row[2].toLowerCase()));
                // Note: customerId and leadId would need to be handled based on business logic
                // For now, we'll leave them as null and they'd need to be set separately
                
                CustomerNoteResponse response = customerNoteService.createNote(request);
                importedNotes.add(response);
            }
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer notes imported successfully", importedNotes));
    }

    @PostMapping("/import/excel")
    public ResponseEntity<ApiResponse<List<CustomerNoteResponse>>> importNotesExcel(
            @RequestParam("file") MultipartFile file) throws Exception {
        List<String[]> rows = ExcelImportUtil.parseXlsx(file.getInputStream());
        List<CustomerNoteResponse> importedNotes = new ArrayList<>();
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length >= 4) {
                CustomerNoteRequest request = new CustomerNoteRequest();
                request.setTitle(row[0]);
                request.setContent(row[1]);
                request.setPinned(Boolean.parseBoolean(row[2].toLowerCase()));
                // Note: customerId and leadId would need to be handled based on business logic
                // For now, we'll leave them as null and they'd need to be set separately
                
                CustomerNoteResponse response = customerNoteService.createNote(request);
                importedNotes.add(response);
            }
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer notes imported successfully", importedNotes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(@PathVariable Long id) {
        customerNoteService.deleteNote(id);
        return ResponseEntity.ok(ApiResponse.success("Note deleted successfully", null));
    }
}
