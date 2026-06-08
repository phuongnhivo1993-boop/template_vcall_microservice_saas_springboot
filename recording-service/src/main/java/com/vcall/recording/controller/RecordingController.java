package com.vcall.recording.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.PagedResponse;
import com.vcall.recording.dto.RecordingRequest;
import com.vcall.recording.dto.RecordingResponse;
import com.vcall.recording.dto.RecordingSearchRequest;
import com.vcall.recording.service.RecordingService;
import java.io.IOException;
import java.net.MalformedURLException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/recordings")
@RequiredArgsConstructor
public class RecordingController {

    private final RecordingService recordingService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<RecordingResponse>> getRecording(@PathVariable UUID id) {
        RecordingResponse response = recordingService.getRecording(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<RecordingResponse>>> searchRecordings(
            @Valid RecordingSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RecordingResponse> recordingPage = recordingService.searchRecordings(request, pageable);

        PagedResponse<RecordingResponse> pagedResponse = PagedResponse.<RecordingResponse>builder()
                .content(recordingPage.getContent())
                .page(recordingPage.getNumber())
                .size(recordingPage.getSize())
                .totalElements(recordingPage.getTotalElements())
                .totalPages(recordingPage.getTotalPages())
                .last(recordingPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Resource> downloadRecording(@PathVariable UUID id) {
        RecordingResponse response = recordingService.getRecording(id);
        String downloadUrl = recordingService.getDownloadUrl(id);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"")
                    .body(new InputStreamResource(new java.net.URL(downloadUrl).openStream()));
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{id}/stream")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Resource> streamRecording(@PathVariable UUID id) {
        RecordingResponse response = recordingService.getRecording(id);
        String downloadUrl = recordingService.getDownloadUrl(id);

        String contentType = switch (response.getFormat().toUpperCase()) {
            case "WAV" -> "audio/wav";
            case "MP3" -> "audio/mpeg";
            case "OGG" -> "audio/ogg";
            default -> "audio/mpeg";
        };

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + response.getFileName() + "\"")
                    .body(new InputStreamResource(new java.net.URL(downloadUrl).openStream()));
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<RecordingResponse>> createRecording(@Valid @RequestBody RecordingRequest request) {
        RecordingResponse response = recordingService.createRecording(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recording created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<RecordingResponse>> updateRecording(
            @PathVariable UUID id, @Valid @RequestBody RecordingRequest request) {
        RecordingResponse response = recordingService.updateRecording(id, request);
        return ResponseEntity.ok(ApiResponse.success("Recording updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteRecording(@PathVariable UUID id) {
        recordingService.deleteRecording(id);
        return ResponseEntity.ok(ApiResponse.success("Recording deleted successfully", null));
    }
}
