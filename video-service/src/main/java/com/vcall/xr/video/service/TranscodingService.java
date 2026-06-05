package com.vcall.xr.video.service;

import com.vcall.xr.video.domain.VideoJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscodingService {

    @Value("${video.ffmpeg.path:/usr/bin/ffmpeg}")
    private String ffmpegPath;

    @Value("${video.ffmpeg.probe-path:/usr/bin/ffprobe}")
    private String ffprobePath;

    @Value("${video.ffmpeg.default-threads:4}")
    private int defaultThreads;

    @Value("${video.ffmpeg.default-crf:23}")
    private int defaultCrf;

    @Value("${video.processing.temp-dir:/tmp/video-processing}")
    private String tempDir;

    private static final Pattern PROGRESS_PATTERN = Pattern.compile("time=(\\d{2}):(\\d{2}):(\\d{2}\\.\\d{2})");

    @Async
    public CompletableFuture<Void> transcodeAsync(VideoJob job, List<String> resolutions,
                                                  VideoJob.ProgressCallback progressCallback) {
        try {
            Path workDir = Files.createTempDirectory(Path.of(tempDir), "transcode-" + job.getId());
            log.info("Starting transcoding job {} in directory {}", job.getId(), workDir);

            probeAndValidate(job.getInputUrl());

            for (String resolution : resolutions) {
                int width = Integer.parseInt(resolution.split("x")[0]);
                int height = Integer.parseInt(resolution.split("x")[1]);

                buildTranscodeCommand(job, width, height, workDir)
                    .ifPresent(cmd -> executeTranscode(job, cmd, progressCallback));
            }

            log.info("Transcoding completed successfully for job {}", job.getId());
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Transcoding failed for job {}: {}", job.getId(), e.getMessage());
            throw new RuntimeException("Transcoding failed: " + e.getMessage(), e);
        }
    }

    private void probeAndValidate(String inputUrl) throws TranscodingException {
        List<String> cmd = List.of(
            ffprobePath,
            "-v", "error",
            "-show_entries", "format=duration,bit_rate:stream=codec_type,codec_name,width,height",
            "-of", "json",
            inputUrl
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new TranscodingException("FFprobe failed with exit code " + exitCode + ": " + output);
            }

            log.debug("FFprobe output: {}", output);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TranscodingException("FFprobe interrupted", e);
        } catch (Exception e) {
            throw new TranscodingException("Failed to run FFprobe: " + e.getMessage(), e);
        }
    }

    private java.util.Optional<List<String>> buildTranscodeCommand(VideoJob job, int width, int height, Path workDir) {
        String outputFile = workDir.resolve(job.getId() + "_" + width + "x" + height + ".mp4").toString();

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-i");
        cmd.add(job.getInputUrl());

        if (job.getVideoType() == com.vcall.xr.video.domain.VideoType.STEREOSCOPIC) {
            cmd.add("-vf");
            cmd.add("stereo3d=sbsl:arcd,scale=" + width + ":" + height);
        } else {
            cmd.add("-vf");
            cmd.add("scale=" + width + ":" + height + ",setsar=1");
        }

        cmd.addAll(List.of(
            "-c:v", "libx264",
            "-preset", "medium",
            "-crf", String.valueOf(defaultCrf),
            "-c:a", "aac",
            "-b:a", "128k",
            "-ac", "2",
            "-threads", String.valueOf(defaultThreads),
            "-movflags", "+faststart",
            "-y",
            outputFile
        ));

        return java.util.Optional.of(cmd);
    }

    private void executeTranscode(VideoJob job, List<String> cmd, VideoJob.ProgressCallback progressCallback) {
        log.info("Executing FFmpeg command: {}", String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (progressCallback != null) {
                    parseProgress(line).ifPresent(progressCallback::updateProgress);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new TranscodingException("FFmpeg exited with code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TranscodingException("FFmpeg transcoding interrupted", e);
        } catch (Exception e) {
            throw new TranscodingException("FFmpeg execution failed: " + e.getMessage(), e);
        }
    }

    private java.util.Optional<Integer> parseProgress(String line) {
        Matcher matcher = PROGRESS_PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                int hours = Integer.parseInt(matcher.group(1));
                int minutes = Integer.parseInt(matcher.group(2));
                double seconds = Double.parseDouble(matcher.group(3));

                double totalSeconds = hours * 3600 + minutes * 60 + seconds;
                return java.util.Optional.of((int) Math.min(99, totalSeconds));
            } catch (NumberFormatException e) {
                log.debug("Could not parse progress from line: {}", line);
            }
        }
        return java.util.Optional.empty();
    }

    private String readProcessOutput(Process process) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public static class TranscodingException extends RuntimeException {
        public TranscodingException(String message) {
            super(message);
        }

        public TranscodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
