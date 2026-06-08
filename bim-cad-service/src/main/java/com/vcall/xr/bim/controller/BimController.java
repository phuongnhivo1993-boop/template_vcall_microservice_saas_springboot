package com.vcall.xr.bim.controller;

import com.vcall.xr.bim.service.BimViewerService;
import com.vcall.xr.bim.service.IfcParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/bim")
@RequiredArgsConstructor
@Tag(name = "BIM/CAD", description = "BIM/CAD file parsing and viewer API")
public class BimController {

    private final IfcParserService ifcParserService;
    private final BimViewerService bimViewerService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Upload an IFC/Revit file")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String tenantId) {
        String objectName = (tenantId != null ? tenantId + "/" : "") + file.getOriginalFilename();
        String result = ifcParserService.uploadIfcFile(objectName, file.getInputStream(), file.getContentType());
        return ResponseEntity.ok(Map.of("objectName", result, "status", "uploaded"));
    }

    @GetMapping("/parse/{objectName:.+}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Parse an IFC file")
    public ResponseEntity<Map<String, Object>> parseIfcFile(@PathVariable String objectName) {
        return ResponseEntity.ok(ifcParserService.parseIfcFile(objectName));
    }

    @GetMapping("/viewer/{objectName:.+}/metadata")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get BIM model metadata for viewer")
    public ResponseEntity<Map<String, Object>> getModelMetadata(@PathVariable String objectName) {
        return ResponseEntity.ok(bimViewerService.getModelMetadata(objectName));
    }

    @GetMapping("/viewer/{objectName:.+}/hierarchy")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get BIM model hierarchy")
    public ResponseEntity<Map<String, Object>> getModelHierarchy(@PathVariable String objectName) {
        return ResponseEntity.ok(bimViewerService.getModelHierarchy(objectName));
    }

    @GetMapping("/viewer/{objectName:.+}/statistics")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get BIM model statistics")
    public ResponseEntity<Map<String, Object>> getModelStatistics(@PathVariable String objectName) {
        return ResponseEntity.ok(bimViewerService.getModelStatistics(objectName));
    }

    @GetMapping("/download/{objectName:.+}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Download a BIM/CAD file")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String objectName) {
        InputStream stream = bimViewerService.getModelStream(objectName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/exists/{objectName:.+}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Check if file exists")
    public ResponseEntity<Map<String, Boolean>> fileExists(@PathVariable String objectName) {
        return ResponseEntity.ok(Map.of("exists", ifcParserService.fileExists(objectName)));
    }
}
