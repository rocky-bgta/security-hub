package com.aspire.asat.vps.service.impl;

import com.aspire.asat.common.service.files.FileService;
import com.aspire.asat.vps.client.ExternalApiService;
import com.aspire.asat.vps.dto.response.RequestDto;
import com.aspire.asat.vps.dto.response.ResponseDto;
import com.aspire.asat.vps.service.AzureStorageService;
import com.aspire.asat.vps.service.VideoProcessService;
import com.aspire.asat.vps.service.VideoUploadService;
import com.aspire.asat.vps.utils.VideoProcessUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class VideoProcessServiceImpl implements VideoProcessService {
    private final ExternalApiService externalApiService;
    private final VideoUploadService videoUploadService;
    private final FileService fileService;

    @Override
    @Async("vpsTaskExecutor")
    public CompletableFuture<ResponseDto> processVideo(RequestDto requestDto) throws Exception {
        System.out.println("Processing video for request: " + requestDto);
        UUID jobId2 = UUID.randomUUID();
        String jobId = jobId2.toString();
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        validateRequest(requestDto);

        String outputDirPath = "output/" + currentDate + "/" + requestDto.getUserId() + "/" + jobId.toString();
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

//        if(VideoProcessUtil.isValidUrl(requestDto.getVideoUrl())) {
//            System.out.println("Video URL is valid");
//        } else {
//            System.out.println("********Video URL is invalid************");
//            throw new IllegalArgumentException("Invalid video URL");
//        }

        String outputManifest = outputDirPath + "/sample.mpd";
        System.out.println("output manifest: " + outputManifest);

        //test start
//        UUID testid = requestDto.getId();
//        String contentSpecificId = requestDto.getSpecificContentId();
//        System.out.println("Received request with id: " + testid + ", contentSpecificId: " + contentSpecificId);
//        String processedVideoUrl = "http://xyz.com/processedVideoUrl";
//        externalApiService.updateInteractiveVideoAttributes(testid, contentSpecificId, processedVideoUrl, "PROCESSED");

        //test end

        // Download source from S3 via SDK (NOT a presigned URL). FFmpeg reads local disk so encode
        // is not limited by 1-hour presign expiry or HTTP range-request 403s during long jobs.
        String sourceKey = requestDto.getVideoUrl();
        String sourceExt = extractExtension(sourceKey);
        Path localSourcePath = Paths.get(outputDirPath, "source." + sourceExt);
        log.info("Downloading source from S3 key={} -> {}", sourceKey, localSourcePath);
        try (InputStream in = fileService.readFile(sourceKey)) {
            Files.copy(in, localSourcePath, StandardCopyOption.REPLACE_EXISTING);
        }
        long sourceBytes = Files.size(localSourcePath);
        if (sourceBytes <= 0) {
            throw new RuntimeException("Empty source download for key=" + sourceKey);
        }
        log.info("Downloaded source: {} bytes ({} MB)", sourceBytes, sourceBytes / (1024 * 1024));

        String localSource = localSourcePath.toString();
        try {
            int exitCode = getExitCode(localSource, outputManifest);

            if (exitCode == 0) {
                double inputSec = probeDurationSeconds(localSource);
                double outputSec = readMpdDurationSeconds(Paths.get(outputManifest));
                double drift = Math.abs(inputSec - outputSec);
                log.info("Duration check for jobId={}: input={}s output={}s drift={}s", jobId, inputSec, outputSec, drift);
                if (inputSec > 0 && outputSec > 0 && drift > 6.0) {
                    throw new RuntimeException(
                            "Output truncated for jobId=" + jobId
                                    + ": input=" + inputSec + "s, output=" + outputSec + "s, drift=" + drift + "s");
                }

                // Delete source before upload walk so raw MP4 is not re-uploaded to S3.
                Files.deleteIfExists(localSourcePath);

                System.out.println("upload the output manifest to azure blob");
                ResponseDto responseDto = videoUploadService.uploadVideosToCloud(outputDirPath, requestDto, jobId, currentDate);
                System.out.println("After uploading to azure blob calling external api: processedVideoUrl: " + responseDto.getProcessedVideoUrl());
                System.out.println("---------------------------" + responseDto.getUploadedFiles().toString());
                externalApiService.updateInteractiveVideoAttributes(requestDto.getId(), requestDto.getSpecificContentId(), responseDto.getProcessedVideoUrl(), "PROCESSED");
                System.out.println("-----END calling external api-----------------");

                return CompletableFuture.completedFuture(ResponseDto.builder()
                        .id(requestDto.getId())
                        .specificContentId(requestDto.getSpecificContentId())
                        .contentType(requestDto.getContentType())
                        .processingStatus("PROCESSED")
                        .videoUrl(responseDto.getVideoUrl())
                        .processedVideoUrl(responseDto.getProcessedVideoUrl())
                        .userId(requestDto.getUserId())
                        .uploadedFiles(responseDto.getUploadedFiles())
                        .uploadedAt(responseDto.getUploadedAt())
                        .build());
            } else {
                System.out.println("ffmpeg command failed with exit code: " + exitCode);
                throw new RuntimeException("ffmpeg command failed with exit code: " + exitCode);
            }
        } finally {
            try {
                Files.deleteIfExists(localSourcePath);
                log.info("Deleted local source {}", localSourcePath);
            } catch (IOException e) {
                log.warn("Failed to delete local source {}: {}", localSourcePath, e.getMessage());
            }
        }
    }

    private void validateRequest(RequestDto requestDto) {
        if (requestDto.getVideoUrl() == null || requestDto.getVideoUrl().isEmpty()) {
            throw new IllegalArgumentException("Blob URL cannot be null or empty");
        }
        if (requestDto.getUserId() == null || requestDto.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    }

    private static int getExitCode(String inputPath, String outputManifest) throws IOException, InterruptedException {
        // Input is a LOCAL FILE (downloaded via AWS SDK). Do NOT add -reconnect* / -rw_timeout /
        // -multiple_requests: those are HTTP protocol options and cause "Option reconnect not found."
        // on the file protocol.
        String[] command = {
                "ffmpeg",
                "-hide_banner", "-loglevel", "info", "-stats",
                "-i", inputPath,

                "-filter_complex",
                "[0:v]split=4[v0][v1][v2][v3];" +
                        "[v0]scale=1920:1080,setdar=16/9[v0out];" +
                        "[v1]scale=1280:720,setdar=16/9[v1out];" +
                        "[v2]scale=1280:720,setdar=16/9[v2out];" +
                        "[v3]scale=640:360,setdar=16/9[v3out]",

                "-map", "[v0out]", "-map", "[v1out]", "-map", "[v2out]", "-map", "[v3out]",
                "-map", "0:a?",

                "-c:v:0", "libx264", "-b:v:0", "3000k", "-profile:v:0", "high",
                "-c:v:1", "libx264", "-b:v:1", "1500k", "-profile:v:1", "main",
                "-c:v:2", "libx264", "-b:v:2", "800k",  "-profile:v:2", "main",
                "-c:v:3", "libx264", "-b:v:3", "300k",  "-profile:v:3", "baseline",

                "-c:a", "aac", "-b:a", "128k", "-ar", "44100",

                "-preset", "superfast",
                "-level", "4.0",
                "-bf", "0",
                "-keyint_min", "60",
                "-g", "60",
                "-sc_threshold", "0",
                "-b_strategy", "0",
                "-use_timeline", "1",
                "-use_template", "1",
                "-adaptation_sets", "id=0,streams=v id=1,streams=a",
                "-seg_duration", "6",
                "-f", "dash",
                outputManifest
        };

        System.out.println("FFmpeg command:\n" + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // Merge stdout and stderr
        Process process = pb.start();

        // Read output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[ffmpeg] " + line);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("Process exited with code: " + exitCode);
        return exitCode;
    }

    private static String extractExtension(String key) {
        if (key == null || key.isBlank()) {
            return "mp4";
        }
        int slash = Math.max(key.lastIndexOf('/'), key.lastIndexOf('\\'));
        int dot = key.lastIndexOf('.');
        if (dot > slash && dot < key.length() - 1) {
            String ext = key.substring(dot + 1).toLowerCase();
            if (ext.matches("[a-z0-9]{1,6}")) {
                return ext;
            }
        }
        return "mp4";
    }

    private static final Pattern MPD_DURATION_PATTERN =
            Pattern.compile("mediaPresentationDuration\\s*=\\s*\"(PT[^\"]+)\"");

    /**
     * Reads duration from DASH MPD XML (mediaPresentationDuration). ffprobe's DASH demuxer fails
     * on local manifests with "Error when loading first fragment of playlist".
     */
    private static double readMpdDurationSeconds(Path mpdPath) {
        try {
            String content = Files.readString(mpdPath);
            Matcher m = MPD_DURATION_PATTERN.matcher(content);
            if (m.find()) {
                return Duration.parse(m.group(1)).toMillis() / 1000.0;
            }
            log.warn("mediaPresentationDuration not found in MPD: {}", mpdPath);
        } catch (IOException e) {
            log.warn("Failed to read MPD {}: {}", mpdPath, e.getMessage());
        } catch (java.time.format.DateTimeParseException e) {
            log.warn("Failed to parse mediaPresentationDuration in MPD {}: {}", mpdPath, e.getMessage());
        }
        return 0.0;
    }

    /**
     * Probes duration of a LOCAL media file via ffprobe. For {@code .mpd} use
     * {@link #readMpdDurationSeconds(Path)}. Returns 0.0 on failure (unknown, not truncated).
     */
    private static double probeDurationSeconds(String inputPath) {
        String[] command = {
                "ffprobe",
                "-v", "error",
                "-hide_banner",
                "-show_entries", "format=duration",
                "-of", "csv=p=0",
                inputPath
        };

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String firstLine = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (firstLine == null && !line.isBlank()) {
                        firstLine = line.trim();
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0 || firstLine == null) {
                log.warn("ffprobe failed for input {} (exit={}, output={})", inputPath, exitCode, firstLine);
                return 0.0;
            }
            return Double.parseDouble(firstLine);
        } catch (NumberFormatException e) {
            log.warn("ffprobe returned non-numeric duration for input {}: {}", inputPath, e.getMessage());
            return 0.0;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("ffprobe process error for input {}: {}", inputPath, e.getMessage());
            return 0.0;
        }
    }


    /*
    private static int getExitCode(RequestDto requestDto, String outputManifest) throws IOException, InterruptedException {

//        String[] command = {
//                "ffmpeg",
//                "-re",
//                "-i", requestDto.getVideoUrl(),
//                "-map", "0", "-map", "0",
//                "-c:a", "copy", // Copy audio as per original command
//                "-c:v", "libx264",
//                "-preset", "superfast", // Use superfast preset as per original command
//                "-b:v:0", "3000k", // Video bitrate for first stream
//                "-b:v:1", "1500k", // Video bitrate for second stream
//                "-b:v:2", "800k",  // Video bitrate for third stream
//                "-b:v:3", "300k",  // Video bitrate for fourth stream
//                "-s:v:0", "1920x1080", // Video size for first stream
//                "-s:v:1", "1280x720", // Video size for second stream
//                "-s:v:2", "1280x720", // Video size for third stream
//                "-s:v:3", "640x360", // Video size for fourth stream
//                "-profile:v:0", "high", // Profile for first stream
//                "-profile:v:1", "main", // Profile for second stream
//                "-profile:v:2", "main", // Profile for third stream
//                "-profile:v:3", "baseline", // Profile for fourth stream
//                "-level", "4.0", // Video level for all streams
//                "-bf", "0", // No B-frames
//                "-keyint_min", "60", // Min keyframe interval
//                "-g", "60", // Keyframe interval
//                "-sc_threshold", "0", // Scene change threshold
//                "-b_strategy", "0", // B-frame strategy
//                "-ar:a:1", "22050", // Audio sample rate
//                "-use_timeline", "1", // Use timeline
//                "-use_template", "1", // Use template
//                "-adaptation_sets", "id=0,streams=0 id=1,streams=1 id=2,streams=2 id=3,streams=3", // Adaptation sets
//                "-f", "dash", // DASH output format
//                "-seg_duration", "6", // Segment duration (as per original command)
//                outputManifest
//        };

        String[] command = {
                "ffmpeg",
                "-re",
                "-i", requestDto.getVideoUrl(),

                // Filter complex for multi-resolution outputs
                "-filter_complex",
                "[0:v]split=4[v0][v1][v2][v3];" +
                        "[v0]scale=1920:1080,setdar=16/9[v0out];" +
                        "[v1]scale=1280:720,setdar=16/9[v1out];" +
                        "[v2]scale=1280:720,setdar=16/9[v2out];" +
                        "[v3]scale=640:360,setdar=16/9[v3out]",

                // Map outputs
                "-map", "[v0out]",
                "-map", "[v1out]",
                "-map", "[v2out]",
                "-map", "[v3out]",
                "-map", "0:a",

                // Video encoding settings
                "-c:v:0", "libx264", "-b:v:0", "3000k", "-profile:v:0", "high",
                "-c:v:1", "libx264", "-b:v:1", "1500k", "-profile:v:1", "main",
                "-c:v:2", "libx264", "-b:v:2", "800k",  "-profile:v:2", "main",
                "-c:v:3", "libx264", "-b:v:3", "300k",  "-profile:v:3", "baseline",

                // Audio encoding (unified)
                "-c:a", "aac",
                "-b:a", "128k",
                "-ar", "44100",

                // Encoding and DASH flags
                "-preset", "superfast",
                "-level", "4.0",
                "-bf", "0",
                "-keyint_min", "60",
                "-g", "60",
                "-sc_threshold", "0",
                "-b_strategy", "0",
                "-use_timeline", "1",
                "-use_template", "1",

                // Adaptation sets: one for video, one for audio
                "-adaptation_sets", "id=0,streams=v id=1,streams=a",
                "-seg_duration", "6",
                "-f", "dash",
                outputManifest
        };



        System.out.println("ffmpeg Command: " + String.join(" ", command));

        try{
            System.out.println("Executing command...");
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            return exitCode;
        } catch (IOException e) {
            System.err.println("Error executing command: " + e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            System.err.println("Process was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw e;
        }

    }
    */

}
