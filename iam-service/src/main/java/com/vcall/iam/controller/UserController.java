package com.vcall.iam.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.BulkStatusRequest;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.iam.dto.UserRequest;
import com.vcall.iam.dto.UserResponse;
import com.vcall.iam.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-username")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@RequestParam String username) {
        UserResponse response = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID id,
                                                                @Valid @RequestBody UserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(@PathVariable UUID id,
                                                                       @RequestParam String status) {
        UserResponse response = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            Pageable pageable) {
        Specification<com.vcall.iam.entity.User> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(root.get("phone"), "%" + keyword + "%")
                    ));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("status").as(String.class)), status.toLowerCase()));
        }
        if (email != null && !email.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (phone != null && !phone.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("phone"), "%" + phone + "%"));
        }
        Page<UserResponse> response = userService.searchUsers(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    public void exportUsersCsv(@RequestParam(required = false) String keyword,
                               HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<UserResponse> users;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<com.vcall.iam.entity.User> spec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")
                    );
            users = userService.searchUsers(spec, pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Username", "Full Name", "Email", "Phone", "Status", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(users.getContent(),
                Arrays.asList("id", "username", "fullName", "email", "phone", "status", "createdAt"));
        CsvExportUtil.writeCsv(response, "users.csv", headers, rows);
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkDelete(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                userService.deleteUser(id);
                result.addSuccess((Object) id);
            } catch (Exception e) {
                result.addFailure((Object) id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkStatus(
            @RequestBody BulkStatusRequest request) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : request.getIds()) {
            try {
                userService.updateUserStatus(id, request.getStatus());
                result.addSuccess((Object) id);
            } catch (Exception e) {
                result.addFailure((Object) id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk status update completed", result));
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<?>>> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {
        List<UserRequest> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 3) {
                    UserRequest request = UserRequest.builder()
                            .username(fields[0].trim())
                            .password(fields[1].trim())
                            .email(fields.length > 2 ? fields[2].trim() : null)
                            .phone(fields.length > 3 ? fields[3].trim() : null)
                            .fullName(fields.length > 4 ? fields[4].trim() : fields[0].trim())
                            .build();
                    items.add(request);
                }
            }
        }
        BulkOperationUtil.BulkResult<?> result = BulkOperationUtil.bulkCreate(items, item ->
                userService.createUser((UserRequest) item));
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }
}
