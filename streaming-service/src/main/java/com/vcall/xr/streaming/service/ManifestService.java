package com.vcall.xr.streaming.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManifestService {

    private final MinioStorageService storageService;

    @Value("${streaming.hls.segment-duration:6}")
    private int hlsSegmentDuration;

    @Value("${streaming.hls.playlist-size:5}")
    private int hlsPlaylistSize;

    @Value("${streaming.hls.target-duration:6}")
    private int hlsTargetDuration;

    @Value("${streaming.hls.master-playlist:master.m3u8}")
    private String hlsMasterPlaylist;

    @Value("${streaming.dash.segment-duration:6}")
    private int dashSegmentDuration;

    @Value("${streaming.dash.minimum-buffer:2}")
    private int dashMinimumBuffer;

    @Value("${streaming.dash.minimum-update-period:1}")
    private int dashMinimumUpdatePeriod;

    @Value("${streaming.dash.manifest:manifest.mpd}")
    private String dashManifest;

    @Value("${streaming.storage.prefix:streams}")
    private String storagePrefix;

    public ManifestResult generateHlsManifest(UUID streamId, List<VideoVariant> variants) {
        log.info("Generating HLS manifest for stream {}", streamId);

        try {
            StringBuilder masterPlaylist = new StringBuilder();
            masterPlaylist.append("#EXTM3U\n");
            masterPlaylist.append("#EXT-X-VERSION:3\n\n");

            for (VideoVariant variant : variants) {
                masterPlaylist.append(String.format(
                    "#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%s,NAME=\"%s\"\n",
                    variant.getBitrate(), variant.getResolution(), variant.getName()
                ));
                masterPlaylist.append(String.format("%s/index.m3u8\n", variant.getName()));
            }

            String masterKey = buildKey(streamId, hlsMasterPlaylist);
            storageService.putObject(masterKey, masterPlaylist.toString().getBytes(StandardCharsets.UTF_8), "application/x-mpegurl");

            for (VideoVariant variant : variants) {
                generateVariantPlaylist(streamId, variant);
            }

            ManifestResult result = new ManifestResult();
            result.setFormat("HLS");
            result.setStreamId(streamId);
            result.setMasterPlaylistUrl(storageService.getPublicUrl(masterKey));
            result.setVariantCount(variants.size());

            log.info("HLS manifest generated successfully for stream {}", streamId);
            return result;

        } catch (Exception e) {
            log.error("Failed to generate HLS manifest for stream {}: {}", streamId, e.getMessage());
            throw new ManifestGenerationException("HLS manifest generation failed: " + e.getMessage(), e);
        }
    }

    private void generateVariantPlaylist(UUID streamId, VideoVariant variant) {
        StringBuilder playlist = new StringBuilder();
        playlist.append("#EXTM3U\n");
        playlist.append("#EXT-X-VERSION:3\n");
        playlist.append(String.format("#EXT-X-TARGETDURATION:%d\n", hlsTargetDuration));
        playlist.append("#EXT-X-MEDIA-SEQUENCE:0\n\n");

        for (int i = 0; i < hlsPlaylistSize; i++) {
            playlist.append(String.format("#EXTINF:%.3f,\n", (double) hlsSegmentDuration));
            playlist.append(String.format("%s/segment_%05d.ts\n", variant.getName(), i));
        }

        playlist.append("#EXT-X-ENDLIST\n");

        String variantKey = buildKey(streamId, variant.getName() + "/index.m3u8");
        storageService.putObject(variantKey, playlist.toString().getBytes(StandardCharsets.UTF_8), "application/x-mpegurl");
    }

    public ManifestResult generateDashManifest(UUID streamId, List<VideoVariant> variants) {
        log.info("Generating DASH manifest for stream {}", streamId);

        try {
            StringBuilder mpd = new StringBuilder();
            mpd.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            mpd.append("<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\"\n");
            mpd.append("     type=\"static\"\n");
            mpd.append("     mediaPresentationDuration=\"PT1H0M0S\"\n");
            mpd.append("     minBufferTime=\"PT").append(dashMinimumBuffer).append("S\"\n");
            mpd.append("     profiles=\"urn:mpeg:dash:profile:isoff-on-demand:2011\"\n");
            mpd.append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            mpd.append("     xsi:schemaLocation=\"urn:mpeg:dash:schema:mpd:2011 DASH-MPD.xsd\">\n\n");
            mpd.append("  <Period id=\"0\" start=\"PT0S\">\n");

            for (VideoVariant variant : variants) {
                mpd.append("    <AdaptationSet id=\"").append(variant.getIndex()).append("\" contentType=\"video\" mimeType=\"video/mp4\"\n");
                mpd.append("                   width=\"").append(variant.getWidth()).append("\" height=\"").append(variant.getHeight()).append("\">\n");
                mpd.append("      <Representation id=\"").append(variant.getName()).append("\" bandwidth=\"").append(variant.getBitrate()).append("\"\n");
                mpd.append("                       codecs=\"avc1.64001f\" frameRate=\"30\">\n");
                mpd.append("        <SegmentTemplate timescale=\"1000\"\n");
                mpd.append("                         duration=\"").append(dashSegmentDuration * 1000).append("\"/>\n");
                mpd.append("        <BaseURL>").append(variant.getName()).append("/</BaseURL>\n");
                mpd.append("        <SegmentList>\n");
                mpd.append("          <SegmentURL media=\"segment_00001.m4s\"/>\n");
                mpd.append("        </SegmentList>\n");
                mpd.append("      </Representation>\n");
                mpd.append("    </AdaptationSet>\n");
            }

            mpd.append("  </Period>\n");
            mpd.append("</MPD>\n");

            String mpdKey = buildKey(streamId, dashManifest);
            storageService.putObject(mpdKey, mpd.toString().getBytes(StandardCharsets.UTF_8), "application/dash+xml");

            ManifestResult result = new ManifestResult();
            result.setFormat("DASH");
            result.setStreamId(streamId);
            result.setMasterPlaylistUrl(storageService.getPublicUrl(mpdKey));
            result.setVariantCount(variants.size());

            log.info("DASH manifest generated successfully for stream {}", streamId);
            return result;

        } catch (Exception e) {
            log.error("Failed to generate DASH manifest for stream {}: {}", streamId, e.getMessage());
            throw new ManifestGenerationException("DASH manifest generation failed: " + e.getMessage(), e);
        }
    }

    private String buildKey(UUID streamId, String filename) {
        return String.format("%s/%s/%s", storagePrefix, streamId, filename);
    }

    @Data
    public static class ManifestResult {
        private String format;
        private UUID streamId;
        private String masterPlaylistUrl;
        private int variantCount;
    }

    @Data
    public static class VideoVariant {
        private String name;
        private int index;
        private int width;
        private int height;
        private int bitrate;
        private String resolution;

        public static VideoVariant of(String name, int index, int width, int height, int bitrate) {
            VideoVariant v = new VideoVariant();
            v.setName(name);
            v.setIndex(index);
            v.setWidth(width);
            v.setHeight(height);
            v.setBitrate(bitrate);
            v.setResolution(width + "x" + height);
            return v;
        }
    }

    public static class ManifestGenerationException extends RuntimeException {
        public ManifestGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
