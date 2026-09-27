package com.aspire.asat.phishing.media.adapter.impl;

import com.aspire.asat.phishing.client.HeyGenAssetUploadResult;
import com.aspire.asat.phishing.client.HeyGenClient;
import com.aspire.asat.phishing.client.HeyGenImageVideoRequest;
import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.HeyGenBackgroundMapper;
import com.aspire.asat.phishing.media.adapter.VideoRenderAdapter;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.media.model.VideoRenderInput;
import com.aspire.asat.phishing.media.model.VideoRenderResult;
import com.aspire.asat.phishing.util.DeepfakeImageUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeyGenVideoRenderAdapter implements VideoRenderAdapter {

    private static final String AUDIO_MPEG = "audio/mpeg";

    private final HeyGenClient heyGenClient;
    private final HeyGenBackgroundMapper heyGenBackgroundMapper;

    @Value("${heygen.poll.max-attempts:180}")
    private int pollMaxAttempts;

    @Value("${heygen.poll.interval-ms:5000}")
    private long pollIntervalMs;

    @Value("${heygen.default-resolution:1080p}")
    private String defaultResolution;

    @Value("${heygen.default-aspect-ratio:16:9}")
    private String defaultAspectRatio;

    @Value("${heygen.default-expressiveness:low}")
    private String defaultExpressiveness;

    @Value("${heygen.default-motion-prompt:}")
    private String defaultMotionPrompt;

    @Override
    public VideoRenderProvider getProvider() {
        return VideoRenderProvider.HEYGEN;
    }

    @Override
    public VideoRenderResult render(VideoRenderInput input) {
        ResolvedProviderCredentials credentials = input.getCredentials();
        AudioSource audioSource = resolveAudioSource(input, credentials);
        FaceSource faceSource = resolveFaceSource(input);

        Map<String, Object> background = heyGenBackgroundMapper.toHeyGenBackground(
                input.getBackgroundType(),
                input.getBackgroundPreset(),
                input.getCustomBackgroundUrl());

        HeyGenImageVideoRequest.HeyGenImageVideoRequestBuilder videoRequest = HeyGenImageVideoRequest.builder()
                .title(input.getTitle())
                .resolution(resolveResolution(input.getModel()))
                .aspectRatio(defaultAspectRatio)
                .expressiveness(defaultExpressiveness)
                .background(background)
                .removeBackground(background != null && !background.isEmpty());

        if (defaultMotionPrompt != null && !defaultMotionPrompt.isBlank()) {
            videoRequest.motionPrompt(defaultMotionPrompt.trim());
        }

        if (audioSource.assetId() != null) {
            videoRequest.audioAssetId(audioSource.assetId());
        } else {
            videoRequest.audioUrl(audioSource.url());
        }

        log.info("Creating HeyGen image video faceDims={}x{} bytes={} facePath={} audioPath={}",
                faceSource.width(),
                faceSource.height(),
                faceSource.bytes() == null ? 0 : faceSource.bytes().length,
                faceSource.path(),
                audioSource.path());

        String videoId = createImageVideoWithFallback(videoRequest, faceSource, credentials);
        HeyGenOutput out = pollUntilReady(videoId, credentials);
        byte[] videoBytes = heyGenClient.downloadVideo(out.videoUrl());

        VideoRenderResult.VideoRenderResultBuilder result = VideoRenderResult.builder()
                .videoBytes(videoBytes)
                .contentType("video/mp4");

        if (out.thumbnailUrl() != null && !out.thumbnailUrl().isBlank()) {
            try {
                result.thumbnailBytes(heyGenClient.downloadBinary(out.thumbnailUrl()))
                        .thumbnailContentType("image/jpeg");
            } catch (Exception e) {
                log.warn("HeyGen thumbnail download failed for videoId={}; continuing without thumbnail",
                        videoId, e);
            }
        }

        return result.build();
    }

    private String createImageVideoWithFallback(
            HeyGenImageVideoRequest.HeyGenImageVideoRequestBuilder baseRequest,
            FaceSource faceSource,
            ResolvedProviderCredentials credentials) {
        ServiceException lastError = null;

        if (faceSource.bytes() != null && faceSource.bytes().length > 0) {
            try {
                String videoId = heyGenClient.createImageVideo(baseRequest
                        .imageBytes(faceSource.bytes())
                        .imageMediaType(DeepfakeImageUtils.JPEG)
                        .imageUrl(null)
                        .build(), credentials);
                log.info("Submitted HeyGen image video via inline base64 face image ({} bytes)",
                        faceSource.bytes().length);
                return videoId;
            } catch (ServiceException e) {
                log.warn("HeyGen image video via base64 failed for facePath={}", faceSource.path(), e);
                lastError = e;
            }
        }

        if (faceSource.url() != null && !faceSource.url().isBlank()) {
            try {
                String videoId = heyGenClient.createImageVideo(baseRequest
                        .imageBytes(null)
                        .imageUrl(faceSource.url())
                        .build(), credentials);
                log.info("Submitted HeyGen image video via face URL");
                return videoId;
            } catch (ServiceException e) {
                log.warn("HeyGen image video via face URL failed for facePath={}", faceSource.path(), e);
                lastError = e;
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new ServiceException("Unable to submit HeyGen image video", HttpStatus.BAD_GATEWAY);
    }

    private FaceSource resolveFaceSource(VideoRenderInput input) {
        if (input.getFaceFile() != null && input.getFaceFile().exists()) {
            try {
                byte[] originalBytes = Files.readAllBytes(input.getFaceFile().toPath());
                byte[] normalizedBytes = DeepfakeImageUtils.normalizeToJpegBytes(originalBytes);
                int[] dims = DeepfakeImageUtils.readImageDimensions(normalizedBytes);
                return new FaceSource(
                        input.getFaceImageUrl(),
                        normalizedBytes,
                        dims == null ? -1 : dims[0],
                        dims == null ? -1 : dims[1],
                        "face_base64");
            } catch (IOException e) {
                throw new ServiceException("Unable to normalize face image for HeyGen rendering",
                        HttpStatus.BAD_REQUEST, e);
            }
        }

        if (input.getFaceImageUrl() != null && !input.getFaceImageUrl().isBlank()) {
            return new FaceSource(input.getFaceImageUrl(), null, -1, -1, "face_url");
        }

        throw new ServiceException("A face image file or URL is required for HeyGen rendering",
                HttpStatus.BAD_REQUEST);
    }

    private AudioSource resolveAudioSource(VideoRenderInput input, ResolvedProviderCredentials credentials) {
        if (input.getAudioFile() != null && input.getAudioFile().exists()) {
            try {
                HeyGenAssetUploadResult audioUpload =
                        heyGenClient.uploadAsset(input.getAudioFile(), AUDIO_MPEG, credentials);
                log.debug("Uploaded HeyGen audio asset {}", audioUpload.getAssetId());
                return new AudioSource(audioUpload.getAssetId(), null, "audio_asset_id");
            } catch (ServiceException e) {
                if (input.getAudioUrl() != null && !input.getAudioUrl().isBlank()) {
                    log.warn("HeyGen audio asset upload failed, falling back to presigned audio URL", e);
                    return new AudioSource(null, input.getAudioUrl(), "audio_url");
                }
                throw e;
            }
        }
        if (input.getAudioUrl() != null && !input.getAudioUrl().isBlank()) {
            return new AudioSource(null, input.getAudioUrl(), "audio_url");
        }
        throw new ServiceException("An audio file or reachable audio URL is required for HeyGen rendering",
                HttpStatus.BAD_REQUEST);
    }

    private String resolveResolution(String model) {
        if (model == null || model.isBlank()) {
            return defaultResolution;
        }
        String normalized = model.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("4k")) {
            return "4k";
        }
        if (normalized.contains("720")) {
            return "720p";
        }
        if (normalized.contains("1080")) {
            return "1080p";
        }
        return defaultResolution;
    }

    private HeyGenOutput pollUntilReady(String videoId, ResolvedProviderCredentials credentials) {
        for (int attempt = 1; attempt <= pollMaxAttempts; attempt++) {
            JsonNode data = heyGenClient.getVideoStatus(videoId, credentials);
            String status = data.path("status").asText("").toLowerCase(Locale.ROOT);
            switch (status) {
                case "completed" -> {
                    String url = data.path("video_url").asText(null);
                    if (url == null || url.isBlank()) {
                        throw new ServiceException("HeyGen completed without a video_url", HttpStatus.BAD_GATEWAY);
                    }
                    return new HeyGenOutput(url, data.path("thumbnail_url").asText(null));
                }
                case "failed" -> {
                    String failureMessage = data.path("failure_message").asText(null);
                    if (failureMessage == null || failureMessage.isBlank()) {
                        failureMessage = data.path("error").asText("unknown error");
                    }
                    throw new ServiceException("HeyGen render failed: " + failureMessage, HttpStatus.BAD_GATEWAY);
                }
                default -> sleep();
            }
        }
        log.error("HeyGen render timed out after {} polls for videoId={}", pollMaxAttempts, videoId);
        throw new ServiceException("It takes more time than expected, please check the video processing status in the Deepfake video library",
                HttpStatus.GATEWAY_TIMEOUT);
    }

    private void sleep() {
        try {
            Thread.sleep(pollIntervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("HeyGen poll interrupted", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private record HeyGenOutput(String videoUrl, String thumbnailUrl) {
    }

    private record FaceSource(String url, byte[] bytes, int width, int height, String path) {
    }

    private record AudioSource(String assetId, String url, String path) {
    }
}
