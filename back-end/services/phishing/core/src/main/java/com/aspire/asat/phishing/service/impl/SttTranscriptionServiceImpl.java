package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.TranscriptionStatus;
import com.aspire.asat.phishing.dto.response.SttStatusResponse;
import com.aspire.asat.phishing.dto.response.SttUploadResponse;
import com.aspire.asat.phishing.dto.sqs.SttTranscriptionMessage;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.TranscriptionEntity;
import com.aspire.asat.phishing.repository.TranscriptionRepository;
import com.aspire.asat.phishing.service.OpenAiService;
import com.aspire.asat.phishing.service.SttS3Service;
import com.aspire.asat.phishing.service.SttSqsService;
import com.aspire.asat.phishing.service.SttTranscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SttTranscriptionServiceImpl implements SttTranscriptionService {

    private static final long MAX_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("wav", "mp3", "m4a", "webm", "ogg", "flac");

    private final SttS3Service sttS3Service;
    private final SttSqsService sttSqsService;
    private final OpenAiService openAiService;
    private final TranscriptionRepository transcriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${retry.backoff-ms:500}")
    private long backoffMs;

    @Override
    public SttUploadResponse handleUpload(MultipartFile audioFile, String languageHint) {
        validateUpload(audioFile);

        UUID audioId = UUID.randomUUID();
        String s3Key = sttS3Service.uploadFile(audioFile, "audio");

        TranscriptionEntity entity = TranscriptionEntity.builder()
                .audioId(audioId)
                .s3Key(s3Key)
                .status(TranscriptionStatus.PENDING)
                .languageHint(normalizeLanguage(languageHint))
                .build();
        transcriptionRepository.save(entity);

        sttSqsService.sendMessage(SttTranscriptionMessage.builder()
                .audioId(audioId)
                .s3Key(s3Key)
                .bucket(bucket)
                .language(entity.getLanguageHint())
                .build());

        return SttUploadResponse.builder()
                .audioId(audioId)
                .status(TranscriptionStatus.PENDING)
                .build();
    }

    @Override
    public SttStatusResponse getStatus(UUID audioId) {
        TranscriptionEntity entity = transcriptionRepository.findByAudioId(audioId)
                .orElseThrow(() -> new ResourceNotFoundException("Audio ID not found: " + audioId));
        return SttStatusResponse.builder()
                .audioId(entity.getAudioId())
                .status(entity.getStatus())
                .transcription(entity.getStatus() == TranscriptionStatus.COMPLETED ? entity.getTranscriptionText() : null)
                .build();
    }

    @Override
    public void processAudio(Message message) {
        SttTranscriptionMessage payload;
        try {
            payload = objectMapper.readValue(message.body(), SttTranscriptionMessage.class);
        } catch (Exception e) {
            throw new ServiceException("Invalid STT queue message", HttpStatus.BAD_REQUEST, e);
        }

        UUID audioId = payload.getAudioId();
        TranscriptionEntity entity = transcriptionRepository.findByAudioId(audioId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcription metadata not found: " + audioId));

        if (entity.getStatus() == TranscriptionStatus.COMPLETED) {
            log.info("audioId={} already completed; skipping duplicate message", audioId);
            return;
        }

        entity.setStatus(TranscriptionStatus.PROCESSING);
        transcriptionRepository.save(entity);

        Exception lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            File tempFile = null;
            try {
                tempFile = sttS3Service.downloadFile(payload.getBucket(), payload.getS3Key());
                String transcript = openAiService.transcribe(tempFile, payload.getLanguage(), audioId.toString());

                entity.setTranscriptionText(transcript);
                entity.setStatus(TranscriptionStatus.COMPLETED);
                entity.setFailureReason(null);
                transcriptionRepository.save(entity);
                log.info("audioId={} transcription completed", audioId);
                return;
            } catch (Exception e) {
                lastError = e;
                log.error("audioId={} transcription attempt {}/{} failed", audioId, attempt, maxAttempts, e);
                if (attempt < maxAttempts) {
                    sleep(backoffMs * (1L << (attempt - 1)));
                }
            } finally {
                cleanupTempFile(audioId, tempFile);
            }
        }

        entity.setStatus(TranscriptionStatus.FAILED);
        entity.setFailureReason(lastError != null ? lastError.getMessage() : "Unknown error");
        transcriptionRepository.save(entity);
        throw new ServiceException("audioId=" + audioId + " transcription failed after retries", HttpStatus.INTERNAL_SERVER_ERROR, lastError);
    }

    private String normalizeLanguage(String languageHint) {
        if (languageHint == null || languageHint.isBlank()) {
            return "en";
        }
        String normalized = languageHint.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "english" -> "en";
            case "spanish" -> "es";
            case "french" -> "fr";
            case "german" -> "de";
            default -> normalized;
        };
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Audio file is required", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ServiceException("File size must be <= 20MB", HttpStatus.BAD_REQUEST);
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new ServiceException("Invalid file name", HttpStatus.BAD_REQUEST);
        }
        String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ServiceException("Invalid file format. Allowed: wav, mp3, m4a", HttpStatus.BAD_REQUEST);
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

    private void cleanupTempFile(UUID audioId, File tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile.toPath());
            log.debug("audioId={} deleted local temp file {}", audioId, tempFile.getAbsolutePath());
        } catch (Exception ex) {
            // Fallback for cases where file handles are still open momentarily.
            tempFile.deleteOnExit();
            log.warn("audioId={} failed to delete temp file {} immediately; scheduled deleteOnExit",
                    audioId, tempFile.getAbsolutePath(), ex);
        }
    }
}
