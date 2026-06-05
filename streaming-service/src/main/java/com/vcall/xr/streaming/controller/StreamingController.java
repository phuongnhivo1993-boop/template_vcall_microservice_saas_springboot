package com.vcall.xr.streaming.controller;

import com.vcall.xr.streaming.service.CdnService;
import com.vcall.xr.streaming.service.ManifestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/streaming")
@RequiredArgsConstructor
@Tag(name = "Streaming", description = "HLS/DASH Manifest and Segment Serving")
public class StreamingController {

    private final ManifestService manifestService;
    private final CdnService cdnService;

    @PostMapping("/streams/{streamId}/manifest/hls")
    @Operation(summary = "Generate HLS manifest for a stream")
    public ResponseEntity<ManifestResponse> generateHlsManifest(
            @PathVariable UUID streamId,
            @RequestBody List<VariantRequest> variants) {

        log.info("Generating HLS manifest for stream {}", streamId);

        List<ManifestService.VideoVariant> videoVariants = variants.stream()
                .map(v -> ManifestService.VideoVariant.of(v.getName(), v.getIndex(), v.getWidth(), v.getHeight(), v.getBitrate()))
                .toList();

        ManifestService.ManifestResult result = manifestService.generateHlsManifest(streamId, videoVariants);

        return ResponseEntity.ok(ManifestResponse.from(result));
    }

    @PostMapping("/streams/{streamId}/manifest/dash")
    @Operation(summary = "Generate DASH manifest for a stream")
    public ResponseEntity<ManifestResponse> generateDashManifest(
            @PathVariable UUID streamId,
            @RequestBody List<VariantRequest> variants) {

        log.info("Generating DASH manifest for stream {}", streamId);

        List<ManifestService.VideoVariant> videoVariants = variants.stream()
                .map(v -> ManifestService.VideoVariant.of(v.getName(), v.getIndex(), v.getWidth(), v.getHeight(), v.getBitrate()))
                .toList();

        ManifestService.ManifestResult result = manifestService.generateDashManifest(streamId, videoVariants);

        return ResponseEntity.ok(ManifestResponse.from(result));
    }

    @GetMapping("/streams/{streamId}/manifest")
    @Operation(summary = "Get stream manifest URLs")
    public ResponseEntity<StreamManifestResponse> getStreamManifest(
            @PathVariable UUID streamId,
            @RequestParam(defaultValue = "hls") String format) {

        log.info("Getting manifest for stream {} format {}", streamId, format);

        StreamManifestResponse response = new StreamManifestResponse();
        response.setStreamId(streamId);
        response.setHlsUrl(cdnService.getCdnUrl(streamId, "master.m3u8"));
        response.setDashUrl(cdnService.getCdnUrl(streamId, "manifest.mpd"));

        CdnService.CdnInfo cdnInfo = cdnService.getCdnInfo(streamId);
        response.setCdnInfo(cdnInfo);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/streams/{streamId}/segment/{variant}/{segmentName}")
    @Operation(summary = "Serve a video segment")
    public void serveSegment(
            @PathVariable UUID streamId,
            @PathVariable String variant,
            @PathVariable String segmentName,
            HttpServletResponse response) throws Exception {

        String path = variant + "/" + segmentName;
        String url = cdnService.getCdnUrl(streamId, path);

        log.debug("Serving segment for stream {}: {}", streamId, path);

        response.sendRedirect(url);
    }

    @GetMapping("/streams/{streamId}/variant/{variant}/playlist.m3u8")
    @Operation(summary = "Serve a variant playlist")
    public void serveVariantPlaylist(
            @PathVariable UUID streamId,
            @PathVariable String variant,
            HttpServletResponse response) throws Exception {

        String url = cdnService.getCdnUrl(streamId, variant + "/index.m3u8");

        log.debug("Serving variant playlist for stream {}: {}", streamId, variant);

        response.sendRedirect(url);
    }

    @GetMapping("/streams/{streamId}/cdn-info")
    @Operation(summary = "Get CDN configuration for a stream")
    public ResponseEntity<CdnService.CdnInfo> getCdnInfo(@PathVariable UUID streamId) {
        CdnService.CdnInfo info = cdnService.getCdnInfo(streamId);
        return ResponseEntity.ok(info);
    }

    @DeleteMapping("/streams/{streamId}/cache")
    @Operation(summary = "Invalidate CDN cache for a stream")
    public ResponseEntity<Void> invalidateCache(@PathVariable UUID streamId) {
        log.info("Invalidating CDN cache for stream {}", streamId);
        cdnService.invalidateCache(streamId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/streams/{streamId}/playback")
    @Operation(summary = "Get playback URLs with CDN headers")
    public ResponseEntity<PlaybackResponse> getPlaybackInfo(
            @PathVariable UUID streamId,
            @RequestParam(defaultValue = "hls") String format) {

        CdnService.CdnHeaders headers = cdnService.getCdnHeaders(streamId);
        String url = cdnService.getManifestWithFallback(streamId, format);

        PlaybackResponse response = new PlaybackResponse();
        response.setStreamId(streamId);
        response.setUrl(url);
        response.setFormat(format);
        response.setCacheControl(headers.getCacheControl());
        response.setAccessControlAllowOrigin(headers.getAccessControlAllowOrigin());

        return ResponseEntity.ok(response);
    }

    @Data
    public static class VariantRequest {
        private String name;
        private int index;
        private int width;
        private int height;
        private int bitrate;
    }

    @Data
    public static class ManifestResponse {
        private String format;
        private UUID streamId;
        private String manifestUrl;
        private int variantCount;

        public static ManifestResponse from(ManifestService.ManifestResult result) {
            ManifestResponse response = new ManifestResponse();
            response.setFormat(result.getFormat());
            response.setStreamId(result.getStreamId());
            response.setManifestUrl(result.getMasterPlaylistUrl());
            response.setVariantCount(result.getVariantCount());
            return response;
        }
    }

    @Data
    public static class StreamManifestResponse {
        private UUID streamId;
        private String hlsUrl;
        private String dashUrl;
        private CdnService.CdnInfo cdnInfo;
    }

    @Data
    public static class PlaybackResponse {
        private UUID streamId;
        private String url;
        private String format;
        private String cacheControl;
        private String accessControlAllowOrigin;
    }
}
