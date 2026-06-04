package com.vcall.customer.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.PagedResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.common.util.ExcelImportUtil;
import com.vcall.customer.dto.CustomerAddressRequest;
import com.vcall.customer.dto.CustomerContactRequest;
import com.vcall.customer.dto.CustomerRequest;
import com.vcall.customer.dto.CustomerResponse;
import com.vcall.customer.entity.CustomerAddress;
import com.vcall.customer.entity.CustomerContact;
import com.vcall.customer.kafka.CustomerEventPublisher;
import com.vcall.customer.service.CustomerAddressService;
import com.vcall.customer.service.CustomerContactService;
import com.vcall.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerContactService customerContactService;
    private final CustomerAddressService customerAddressService;
    private final CustomerEventPublisher customerEventPublisher;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dateFrom,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dateTo) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<CustomerResponse> result = customerService.findAll(keyword, gender, company, nationality, dateFrom, dateTo, PageRequest.of(page, size, sort));
        PagedResponse<CustomerResponse> paged = PagedResponse.<CustomerResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CustomerResponse> result = customerService.search(keyword, PageRequest.of(page, size));
        PagedResponse<CustomerResponse> paged = PagedResponse.<CustomerResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.create(request);
        customerEventPublisher.publishCustomerCreated(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Customer created", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable UUID id,
                                                                 @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.update(id, request);
        customerEventPublisher.publishCustomerUpdated(response);
        return ResponseEntity.ok(ApiResponse.success("Customer updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted", null));
    }

    @PostMapping("/{id}/contacts")
    public ResponseEntity<ApiResponse<CustomerContact>> addContact(@PathVariable UUID id,
                                                                    @Valid @RequestBody CustomerContactRequest request) {
        CustomerContact contact = customerContactService.addContact(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Contact added", contact));
    }

    @PutMapping("/{customerId}/contacts/{contactId}")
    public ResponseEntity<ApiResponse<CustomerContact>> updateContact(@PathVariable UUID customerId,
                                                                       @PathVariable Long contactId,
                                                                       @Valid @RequestBody CustomerContactRequest request) {
        CustomerContact contact = customerContactService.updateContact(contactId, request);
        return ResponseEntity.ok(ApiResponse.success("Contact updated", contact));
    }

    @DeleteMapping("/{customerId}/contacts/{contactId}")
    public ResponseEntity<ApiResponse<Void>> removeContact(@PathVariable UUID customerId,
                                                            @PathVariable Long contactId) {
        customerContactService.removeContact(contactId);
        return ResponseEntity.ok(ApiResponse.success("Contact removed", null));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<CustomerAddress>> addAddress(@PathVariable UUID id,
                                                                     @Valid @RequestBody CustomerAddressRequest request) {
        CustomerAddress address = customerAddressService.addAddress(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Address added", address));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<CustomerAddress>> updateAddress(@PathVariable UUID customerId,
                                                                       @PathVariable Long addressId,
                                                                       @Valid @RequestBody CustomerAddressRequest request) {
        CustomerAddress address = customerAddressService.updateAddress(addressId, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated", address));
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> removeAddress(@PathVariable UUID customerId,
                                                             @PathVariable Long addressId) {
        customerAddressService.removeAddress(addressId);
        return ResponseEntity.ok(ApiResponse.success("Address removed", null));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam(defaultValue = "") String keyword,
                          HttpServletResponse response) throws IOException {
        List<CustomerResponse> customers;
        if (!keyword.isEmpty()) {
            customers = customerService.search(keyword, PageRequest.of(0, 10000)).getContent();
        } else {
            customers = customerService.findAll(PageRequest.of(0, 10000, Sort.by("createdAt").descending())).getContent();
        }
        List<String> headers = Arrays.asList("ID", "Customer Code", "Full Name", "Email", "Phone", "Company", "Gender", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(customers, Arrays.asList("id", "customerCode", "fullName", "email", "phone", "company", "gender", "createdAt"));
        CsvExportUtil.writeCsv(response, "customers.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(defaultValue = "") String keyword,
                            HttpServletResponse response) throws IOException {
        List<CustomerResponse> customers;
        if (!keyword.isEmpty()) {
            customers = customerService.search(keyword, PageRequest.of(0, 10000)).getContent();
        } else {
            customers = customerService.findAll(PageRequest.of(0, 10000, Sort.by("createdAt").descending())).getContent();
        }
        List<String> headers = Arrays.asList("ID", "Customer Code", "Full Name", "Email", "Phone", "Company", "Gender", "Created At");
        ExcelExportUtil.writeExcel(response, "customers.xlsx", headers, customers,
                Arrays.asList("id", "customerCode", "fullName", "email", "phone", "company", "gender", "createdAt"));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkDelete(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                customerService.delete(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkStatus(
            @RequestBody List<UUID> ids, @RequestParam String status) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                // Customers don't have a mutable status field;
                // this endpoint is a no-op that validates the customer exists.
                customerService.findById(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk status check completed", result));
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<?>>> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {
        List<CustomerRequest> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 1 && !fields[0].trim().isEmpty()) {
                    CustomerRequest request = new CustomerRequest();
                    request.setFullName(fields[0].trim());
                    if (fields.length > 1) request.setEmail(fields[1].trim());
                    if (fields.length > 2) request.setPhone(fields[2].trim());
                    if (fields.length > 3) request.setGender(fields[3].trim());
                    if (fields.length > 4) request.setCompany(fields[4].trim());
                    if (fields.length > 5) request.setPosition(fields[5].trim());
                    if (fields.length > 6) request.setNationality(fields[6].trim());
                    if (fields.length > 7) request.setNotes(fields[7].trim());
                    items.add(request);
                }
            }
        }
        BulkOperationUtil.BulkResult<?> result = BulkOperationUtil.bulkCreate(items, item ->
                customerService.create((CustomerRequest) item));
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }

    @PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<?>>> importExcel(
            @RequestParam("file") MultipartFile file) throws IOException {
        List<CustomerRequest> items = new ArrayList<>();
        try {
            List<String[]> rows = ExcelImportUtil.parseXlsx(file.getInputStream());
            for (int i = 1; i < rows.size(); i++) { // skip header row
                String[] fields = rows.get(i);
                if (fields.length >= 1 && !fields[0].trim().isEmpty()) {
                    CustomerRequest request = new CustomerRequest();
                    request.setFullName(fields[0].trim());
                    if (fields.length > 1) request.setEmail(fields[1].trim());
                    if (fields.length > 2) request.setPhone(fields[2].trim());
                    if (fields.length > 3) request.setGender(fields[3].trim());
                    if (fields.length > 4) request.setCompany(fields[4].trim());
                    if (fields.length > 5) request.setPosition(fields[5].trim());
                    if (fields.length > 6) request.setNationality(fields[6].trim());
                    if (fields.length > 7) request.setNotes(fields[7].trim());
                    items.add(request);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse Excel file", e);
        }
        BulkOperationUtil.BulkResult<?> result = BulkOperationUtil.bulkCreate(items, item ->
                customerService.create((CustomerRequest) item));
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }
}
