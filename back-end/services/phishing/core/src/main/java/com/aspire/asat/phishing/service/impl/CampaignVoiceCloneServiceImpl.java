package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.enums.VoiceCloneSource;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.exception.VoiceCloneLimitReachedException;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import com.aspire.asat.phishing.repository.DeepfakeVoiceCloneRepository;
import com.aspire.asat.phishing.service.CampaignVoiceCloneService;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignVoiceCloneServiceImpl implements CampaignVoiceCloneService {

    private static final long MAX_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("wav", "mp3", "m4a", "webm", "ogg", "flac");

    private final DeepfakeS3Service deepfakeS3Service;
    private final VoiceCloneAdapterFactory voiceCloneAdapterFactory;
    private final DeepfakeVoiceCloneRepository voiceCloneRepository;
    private final ProviderCredentialResolver providerCredentialResolver;

    @Value("${deepfake.voice-clone.reuse-on-limit-reached:false}")
    private boolean reuseOnLimitReached;

    @Override
    public DeepfakeVoiceClone createClone(MultipartFile audioSample,
                                          VoiceCloneProvider provider,
                                          String language,
                                          String clientId,
                                          String voiceName) {
        validate(audioSample);
        String resolvedVoiceName = normalizeOptionalVoiceName(voiceName);

        VoiceCloneProvider resolvedProvider = provider != null ? provider : VoiceCloneProvider.ELEVENLABS;
        String resolvedLanguage = normalizeLanguage(language);
        UUID voiceCloneId = UUID.randomUUID();
        String sampleKey = deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample");

        DeepfakeVoiceClone entity = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(clientId)
                .provider(resolvedProvider)
                .voiceCloneSource(VoiceCloneSource.VISHING)
                .sampleS3Key(sampleKey)
                .voiceName(resolvedVoiceName)
                .sampleFileName(audioSample.getOriginalFilename())
                .language(resolvedLanguage)
                .status(DeepfakeJobStatus.PROCESSING)
                .build();
        voiceCloneRepository.save(entity);

        File tempFile = null;
        try {
            tempFile = deepfakeS3Service.downloadToTemp(sampleKey);
            VoiceCloneAdapter adapter;
            try {
                adapter = voiceCloneAdapterFactory.getAdapter(resolvedProvider);
            } catch (IllegalArgumentException ex) {
                throw new ServiceException(ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
            }
            ResolvedProviderCredentials credentials = providerCredentialResolver.resolve(
                    clientId, resolvedProvider.name());
            String externalVoiceId = resolveExternalVoiceId(
                    adapter, tempFile, resolvedProvider, entity, resolvedLanguage, credentials);
            if (externalVoiceId == null || externalVoiceId.isBlank()) {
                throw new ServiceException("Voice provider did not return a voice id", HttpStatus.BAD_GATEWAY);
            }

            entity.setExternalVoiceId(externalVoiceId);
            entity.setStatus(DeepfakeJobStatus.COMPLETED);
            entity.setFailureReason(null);
            return voiceCloneRepository.save(entity);
        } catch (Exception e) {
            entity.setStatus(DeepfakeJobStatus.FAILED);
            entity.setFailureReason(e.getMessage());
            voiceCloneRepository.save(entity);
            log.error("voiceCloneId={} vishing voice cloning failed", voiceCloneId, e);
            throw e instanceof ServiceException se
                    ? se
                    : new ServiceException("Voice cloning failed: " + e.getMessage(), HttpStatus.BAD_GATEWAY, e);
        } finally {
            cleanup(tempFile);
        }
    }

    @Override
    public DeepfakeVoiceClone getExistingClone(UUID voiceCloneId, String clientId) {
        DeepfakeVoiceClone clone = voiceCloneRepository.findByVoiceCloneId(voiceCloneId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Cloned voice not found: " + voiceCloneId));
        if (!clientId.equals(clone.getClientId())) {
            throw new ResourceNotFoundException("Cloned voice not found: " + voiceCloneId);
        }
        if (clone.getStatus() != DeepfakeJobStatus.COMPLETED
                || clone.getExternalVoiceId() == null || clone.getExternalVoiceId().isBlank()) {
            throw new ServiceException("Selected voice is not ready for reuse", HttpStatus.CONFLICT);
        }
        return clone;
    }

    /**
     * Creates a fresh clone for the sample. When the provider rejects the request
     * because the account's custom-voice quota is exhausted, optionally reuses the
     * most recent successfully cloned voice for the same provider when
     * {@code deepfake.voice-clone.reuse-on-limit-reached} is enabled; otherwise fails loudly.
     */
    private String resolveExternalVoiceId(VoiceCloneAdapter adapter, File sampleFile,
                                          VoiceCloneProvider provider, DeepfakeVoiceClone entity,
                                          String language, ResolvedProviderCredentials credentials) {
        try {
            return adapter.createClone(sampleFile, "vishing-" + entity.getVoiceCloneId(), language, credentials);
        } catch (VoiceCloneLimitReachedException e) {
            if (!reuseOnLimitReached) {
                throw new ServiceException(
                        "Voice cloning limit reached for " + provider
                                + "; enable deepfake.voice-clone.reuse-on-limit-reached or free a custom voice slot",
                        HttpStatus.SERVICE_UNAVAILABLE, e);
            }
            String reused = voiceCloneRepository
                    .findFirstByProviderAndStatusAndExternalVoiceIdIsNotNullOrderByCreatedAtDesc(
                            provider, DeepfakeJobStatus.COMPLETED)
                    .map(DeepfakeVoiceClone::getExternalVoiceId)
                    .orElse(null);
            if (reused == null) {
                throw new ServiceException("Voice cloning limit reached and no previously cloned "
                        + provider + " voice is available to reuse", HttpStatus.SERVICE_UNAVAILABLE, e);
            }
            entity.setUsedFallbackVoice(true);
            log.warn("voiceCloneId={} reusing externalVoiceId={} because {} voice limit is reached",
                    entity.getVoiceCloneId(), reused, provider);
            return reused;
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Audio sample is required", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ServiceException("Audio size must be <= 20MB", HttpStatus.BAD_REQUEST);
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new ServiceException("Invalid file name", HttpStatus.BAD_REQUEST);
        }
        String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ServiceException("Invalid audio format. Allowed: wav, mp3, m4a, webm, ogg, flac",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeOptionalVoiceName(String voiceName) {
        if (voiceName == null || voiceName.isBlank()) {
            return null;
        }
        String trimmed = voiceName.trim();
        // Defense: if Spring already joined identical repeated form fields ("name,name"), collapse them.
        String[] parts = trimmed.split("\\s*,\\s*");
        if (parts.length > 1) {
            boolean allSame = true;
            for (int i = 1; i < parts.length; i++) {
                if (!parts[0].equals(parts[i])) {
                    allSame = false;
                    break;
                }
            }
            if (allSame) {
                return parts[0];
            }
        }
        return trimmed;
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "en";
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private void cleanup(File tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile.toPath());
        } catch (Exception ex) {
            tempFile.deleteOnExit();
        }
    }
}
