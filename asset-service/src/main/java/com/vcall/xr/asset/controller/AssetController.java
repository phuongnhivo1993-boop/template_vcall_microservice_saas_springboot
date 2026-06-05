package com.vcall.xr.asset.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.xr.asset.domain.Asset;
import com.vcall.xr.asset.domain.AssetType;
import com.vcall.xr.asset.domain.TranscodeStatus;
import com.vcall.xr.asset.service.AssetService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CONTENT_MANAGER')")
    @CircuitBreaker(name = "assetUpload", fallbackMethod = "uploadFallback")
    @RateLimiter(name = "assetUpload")
    public ResponseEntity<ApiResponse<Asset>> uploadAsset(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("type") AssetType type,
            @RequestParam(value = "videoType", required = false) String videoType,
            @RequestParam(value = "resolution", required = false) String resolution,
            @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds,
            @RequestParam(value = "modelFormat", required = false) String modelFormat,
            @RequestParam(value = "hasDracoCompression", required = false) Boolean hasDracoCompression,
            @RequestParam(value = "hasKtx2Textures", required = false) Boolean hasKtx2Textures) {

        UUID tenantId = UUID.randomUUID();

        Asset asset;
        switch (type) {
            case VIDEO_360:
                asset = assetService.uploadVideo360(file, name, tenantId, videoType, resolution, durationSeconds);
                break;
            case MODEL_3D:
                boolean draco = hasDracoCompression != null ? hasDracoCompression : false;
                boolean ktx2 = hasKtx2Textures != null ? hasKtx2Textures : false;
                asset = assetService.uploadModel3D(file, name, tenantId, modelFormat, draco, ktx2);
                break;
            default:
                asset = assetService.uploadAsset(file, name, type, tenantId, null);
                break;
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset uploaded successfully", asset));
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "assetService", fallbackMethod = "getAssetFallback")
    public ResponseEntity<ApiResponse<Asset>> getAsset(@PathVariable UUID id) {
        Asset asset = assetService.getAssetById(id);
        return ResponseEntity.ok(ApiResponse.success(asset));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Asset>>> getAssets(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) AssetType type,
            Pageable pageable) {

        Page<Asset> assets;
        if (tenantId != null && type != null) {
            assets = assetService.getAssetsByTenantAndType(tenantId, type, pageable);
        } else if (tenantId != null) {
            assets = assetService.getAssetsByTenant(tenantId, pageable);
        } else {
            assets = assetService.searchAssets(Specification.where(null), pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(assets));
    }

    @GetMapping("/{id}/streaming-url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getStreamingUrl(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "original") String quality) {
        String url = assetService.getStreamingUrl(id, quality);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url)));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CONTENT_MANAGER')")
    @CircuitBreaker(name = "transcodingService", fallbackMethod = "processFallback")
    public ResponseEntity<ApiResponse<Asset>> processAsset(@PathVariable UUID id) {
        Asset asset = assetService.processAsset(id);
        return ResponseEntity.ok(ApiResponse.success("Asset processing started", asset));
    }

    @PatchMapping("/{id}/transcode-status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<ApiResponse<Asset>> updateTranscodeStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> statusUpdate) {
        TranscodeStatus status = TranscodeStatus.valueOf((String) statusUpdate.get("status"));
        int progress = statusUpdate.containsKey("progress") ?
                (Integer) statusUpdate.get("progress") : 0;

        Asset asset = assetService.updateTranscodeStatus(id, status, progress);
        return ResponseEntity.ok(ApiResponse.success("Transcode status updated", asset));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssetStats(
            @RequestParam UUID tenantId) {
        Map<String, Object> stats = assetService.getAssetStats(tenantId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable UUID id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully", null));
    }

    private ResponseEntity<ApiResponse<Asset>> uploadFallback(MultipartFile file, String name,
                                                               AssetType type, String videoType,
                                                               String resolution, Integer durationSeconds,
                                                               String modelFormat, Boolean hasDracoCompression,
                                                               Boolean hasKtx2Textures, Throwable t) {
        log.error("Asset upload failed due to: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Asset service temporarily unavailable. Please try again later."));
    }

    private ResponseEntity<ApiResponse<Asset>> getAssetFallback(UUID id, Throwable t) {
        log.error("Failed to get asset due to: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Asset service temporarily unavailable"));
    }

    private ResponseEntity<ApiResponse<Asset>> processFallback(UUID id, Throwable t) {
        log.error("Asset processing failed due to: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Transcoding service temporarily unavailable"));
    }
}
