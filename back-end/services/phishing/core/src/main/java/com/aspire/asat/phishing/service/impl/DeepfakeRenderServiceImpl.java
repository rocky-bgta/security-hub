package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStatusResponse;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.dto.sqs.DeepfakeRenderMessage;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.adapter.VideoRenderAdapter;
import com.aspire.asat.phishing.media.adapter.VideoRenderAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.media.model.VideoRenderInput;
import com.aspire.asat.phishing.media.model.VideoRenderResult;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.service.DeepfakeRenderService;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.DeepfakeSqsService;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.util.DeepfakeScriptUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeepfakeRenderServiceImpl implements DeepfakeRenderService {

    private final DeepfakeRenderJobRepository renderJobRepository;
    private final DeepfakeS3Service deepfakeS3Service;
    private final DeepfakeSqsService deepfakeSqsService;
    private final VideoRenderAdapterFactory videoRenderAdapterFactory;
    private final UserCurrentContextService userCurrentContextService;
    private final ProviderCredentialResolver providerCredentialResolver;
    private final ObjectMapper objectMapper;

    @Value("${deepfake.retry.max-attempts:2}")
    private int maxAttempts;

    @Value("${deepfake.retry.backoff-ms:1000}")
    private long backoffMs;

    @Value("${deepfake.render.stale-processing-minutes:30}")
    private long staleProcessingMinutes;

    @Override
    public DeepfakeVideoStepResponse enqueueRender(DeepfakeRenderJob job) {
        validateReadyForGenerate(job);

        job.setStatus(DeepfakeJobStatus.PENDING);
        job.setVideoUrl(null);
        job.setVideoS3Key(null);
        job.setThumbnailS3Key(null);
        job.setFailureReason(null);
        DeepfakeRenderJob saved = renderJobRepository.save(job);

        deepfakeSqsService.sendMessage(DeepfakeRenderMessage.builder()
                .renderId(saved.getRenderId().toString())
                .bucket(deepfakeS3Service.getBucket())
                .build());

        return DeepfakeVideoStepResponse.builder()
                .videoId(saved.getRenderId().toString())
                .currentStep(saved.getCurrentStep())
                .status(DeepfakeJobStatus.PENDING)
                .build();
    }

    @Override
    public DeepfakeVideoStatusResponse getVideoStatus(UUID videoId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        DeepfakeRenderJob job = renderJobRepository.findByRenderIdAndClientId(videoId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Deepfake video not found: " + videoId));
        return DeepfakeVideoStatusResponse.builder()
                .videoId(job.getRenderId().toString())
                .status(job.getStatus())
                .videoUrl(resolveVideoUrl(job))
                .thumbnailUrl(resolveThumbnailUrl(job))
                .failureReason(job.getFailureReason())
                .build();
    }

    private String resolveThumbnailUrl(DeepfakeRenderJob job) {
        if (job.getStatus() != DeepfakeJobStatus.COMPLETED) {
            return null;
        }
        return presignSafe(job.getThumbnailS3Key());
    }

    private String resolveVideoUrl(DeepfakeRenderJob job) {
        if (job.getStatus() != DeepfakeJobStatus.COMPLETED) {
            return null;
        }
        if (job.getVideoS3Key() != null && !job.getVideoS3Key().isBlank()) {
            return presignSafe(job.getVideoS3Key());
        }
        return job.getVideoUrl();
    }

    private String presignSafe(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return null;
        }
        try {
            return deepfakeS3Service.presignGetUrl(s3Key);
        } catch (Exception e) {
            log.warn("Failed to presign S3 key {}", s3Key, e);
            return null;
        }
    }

    @Override
    public boolean processRender(Message message) {
        DeepfakeRenderMessage payload;
        try {
            payload = objectMapper.readValue(message.body(), DeepfakeRenderMessage.class);
        } catch (Exception e) {
            throw new ServiceException("Invalid deepfake render queue message", HttpStatus.BAD_REQUEST, e);
        }

        UUID renderId = UUID.fromString(payload.getRenderId());
        DeepfakeRenderJob job = renderJobRepository.findByRenderId(renderId)
                .orElseThrow(() -> new ResourceNotFoundException("Render job metadata not found: " + renderId));

        if (job.getStatus() == DeepfakeJobStatus.COMPLETED) {
            log.info("renderId={} already completed; skipping duplicate message", renderId);
            return true;
        }
        if (job.getStatus() == DeepfakeJobStatus.PROCESSING && !isStaleProcessing(job)) {
            log.info("renderId={} already processing; deferring duplicate message", renderId);
            return false;
        }
        if (job.getStatus() == DeepfakeJobStatus.PROCESSING) {
            log.warn("renderId={} processing appears stale; retrying render", renderId);
        }

        job.setStatus(DeepfakeJobStatus.PROCESSING);
        job.setFailureReason(null);
        renderJobRepository.save(job);

        Exception lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                runRenderPipeline(job);
                return true;
            } catch (Exception e) {
                lastError = e;
                log.error("renderId={} render attempt {}/{} failed", renderId, attempt, maxAttempts, e);
                if (isMissingHeyGenImageDimensions(e)) {
                    job.setHeygenAvatarId(null);
                    job.setHeygenAvatarFaceKey(null);
                    renderJobRepository.save(job);
                }
                if (attempt < maxAttempts) {
                    sleep(backoffMs * (1L << (attempt - 1)));
                }
            }
        }

        job.setStatus(DeepfakeJobStatus.FAILED);
        job.setFailureReason(lastError != null ? lastError.getMessage() : "Unknown error");
        renderJobRepository.save(job);
        throw new ServiceException("renderId=" + renderId + " render failed after retries",
                HttpStatus.INTERNAL_SERVER_ERROR, lastError);
    }

    private void runRenderPipeline(DeepfakeRenderJob job) {
        String audioKey = job.getAudioS3Key();
        if (audioKey == null || audioKey.isBlank()) {
            throw new ServiceException("Generated audio is required for video rendering", HttpStatus.BAD_REQUEST);
        }

        String script = DeepfakeScriptUtils.substituteVariables(job.getScript(), job.getVariables());

        File faceFile = null;
        File audioFile = null;
        try {
            faceFile = deepfakeS3Service.downloadToTemp(job.getFaceKey());
            audioFile = deepfakeS3Service.downloadToTemp(audioKey);
            String faceImageUrl = deepfakeS3Service.presignHeyGenCompatibleFaceUrl(job.getFaceKey());
            String audioUrl = deepfakeS3Service.presignGetUrl(audioKey);
            String customBackgroundUrl = job.getBackgroundType() == DeepfakeBackgroundType.CUSTOM
                    && job.getBackgroundKey() != null && !job.getBackgroundKey().isBlank()
                    ? deepfakeS3Service.presignGetUrl(job.getBackgroundKey())
                    : null;

            VideoRenderAdapter videoAdapter = videoRenderAdapterFactory.getAdapter(job.getVideoProvider());
            ResolvedProviderCredentials credentials = resolveVideoCredentials(job);
            VideoRenderResult result = videoAdapter.render(VideoRenderInput.builder()
                    .faceFile(faceFile)
                    .audioFile(audioFile)
                    .faceImageUrl(faceImageUrl)
                    .audioUrl(audioUrl)
                    .faceKey(job.getFaceKey())
                    .title(job.getTitle())
                    .model(job.getModel())
                    .backgroundType(job.getBackgroundType())
                    .backgroundPreset(job.getBackgroundPreset())
                    .customBackgroundUrl(customBackgroundUrl)
                    .script(script)
                    .credentials(credentials)
                    .build());

            if (result.getHeygenAvatarId() != null && !result.getHeygenAvatarId().isBlank()) {
                job.setHeygenAvatarId(result.getHeygenAvatarId());
                job.setHeygenAvatarFaceKey(result.getHeygenAvatarFaceKey());
            } else {
                job.setHeygenAvatarId(null);
                job.setHeygenAvatarFaceKey(null);
            }

            String contentType = result.getContentType() != null ? result.getContentType() : "video/mp4";
            byte[] videoBytes;
            if (result.getVideoBytes() != null && result.getVideoBytes().length > 0) {
                videoBytes = result.getVideoBytes();
            } else if (result.getVideoUrl() != null && !result.getVideoUrl().isBlank()) {
                videoBytes = downloadVideoFromUrl(result.getVideoUrl());
            } else {
                throw new ServiceException("Video render produced no output", HttpStatus.BAD_GATEWAY);
            }
            String videoKey = deepfakeS3Service.uploadBytes(videoBytes, "deepfake/video", "mp4", contentType);
            job.setVideoS3Key(videoKey);

            if (result.getThumbnailBytes() != null && result.getThumbnailBytes().length > 0) {
                try {
                    String thumbnailKey = deepfakeS3Service.uploadBytes(result.getThumbnailBytes(),
                            "deepfake/thumbnail", "jpg",
                            result.getThumbnailContentType() != null
                                    ? result.getThumbnailContentType() : "image/jpeg");
                    job.setThumbnailS3Key(thumbnailKey);
                } catch (Exception e) {
                    log.warn("renderId={} thumbnail upload failed; completing video without thumbnail",
                            job.getRenderId(), e);
                }
            }

            job.setStatus(DeepfakeJobStatus.COMPLETED);
            job.setFailureReason(null);
            renderJobRepository.save(job);
            log.info("renderId={} render completed", job.getRenderId());
        } finally {
            cleanup(faceFile);
            cleanup(audioFile);
        }
    }

    private byte[] downloadVideoFromUrl(String url) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException("Video download failed with status " + response.statusCode(),
                        HttpStatus.BAD_GATEWAY);
            }
            byte[] bytes = response.body();
            if (bytes == null || bytes.length == 0) {
                throw new ServiceException("Video download returned empty body", HttpStatus.BAD_GATEWAY);
            }
            return bytes;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Video download failed: " + e.getMessage(), HttpStatus.BAD_GATEWAY, e);
        }
    }

    private boolean isMissingHeyGenImageDimensions(Exception error) {
        String message = error == null ? null : error.getMessage();
        if (message == null || message.isBlank()) {
            return false;
        }
        return message.toLowerCase(java.util.Locale.ROOT).contains("missing image dimensions");
    }

    private boolean isStaleProcessing(DeepfakeRenderJob job) {
        Instant updatedAt = job.getUpdatedAt();
        if (updatedAt == null) {
            return true;
        }
        return updatedAt.isBefore(Instant.now().minus(staleProcessingMinutes, ChronoUnit.MINUTES));
    }

    private ResolvedProviderCredentials resolveVideoCredentials(DeepfakeRenderJob job) {
        if (StringUtils.hasText(job.getVideoProviderCredentialId())) {
            return providerCredentialResolver.resolveById(job.getClientId(), job.getVideoProviderCredentialId());
        }
        if (job.getVideoProvider() != null) {
            return providerCredentialResolver.resolve(job.getClientId(), job.getVideoProvider().name());
        }
        return null;
    }

    private void validateReadyForGenerate(DeepfakeRenderJob job) {
        if (job.getCurrentStep() < 6) {
            throw new ServiceException("Complete all wizard steps before generating", HttpStatus.CONFLICT);
        }
        if (job.getFaceKey() == null || job.getFaceKey().isBlank()) {
            throw new ServiceException("faceKey is required", HttpStatus.BAD_REQUEST);
        }
        if (!job.isFaceConfirmed()) {
            throw new ServiceException("Face must be confirmed before generating", HttpStatus.CONFLICT);
        }
        if (job.getScript() == null || job.getScript().isBlank()) {
            throw new ServiceException("script is required", HttpStatus.BAD_REQUEST);
        }
        if (job.getAudioS3Key() == null || job.getAudioS3Key().isBlank()) {
            throw new ServiceException("Generated audio is required before rendering", HttpStatus.BAD_REQUEST);
        }
        if (job.getBackgroundType() == null) {
            throw new ServiceException("background is required", HttpStatus.BAD_REQUEST);
        }
        if (job.getVideoProvider() == null) {
            throw new ServiceException("videoProvider is required", HttpStatus.BAD_REQUEST);
        }
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Retry backoff interrupted", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void cleanup(File tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            java.nio.file.Files.deleteIfExists(tempFile.toPath());
        } catch (Exception ex) {
            tempFile.deleteOnExit();
        }
    }
}
