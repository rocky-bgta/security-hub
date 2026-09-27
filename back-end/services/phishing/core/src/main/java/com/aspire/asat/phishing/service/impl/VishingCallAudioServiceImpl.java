package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.VishingAudioCache;
import com.aspire.asat.phishing.repository.VishingAudioCacheRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.service.VishingCallAudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class VishingCallAudioServiceImpl implements VishingCallAudioService {

    private static final String AUDIO_KEY_PREFIX = "vishing/audio";
    private static final String AUDIO_EXTENSION = "mp3";
    private static final String AUDIO_CONTENT_TYPE = "audio/mpeg";
    private static final String DEFAULT_LANGUAGE = "en";

    private final VoiceCloneAdapterFactory voiceCloneAdapterFactory;
    private final ProviderCredentialResolver providerCredentialResolver;
    private final DeepfakeS3Service deepfakeS3Service;
    private final VishingAudioCacheRepository audioCacheRepository;

    @Override
    public String synthesizeForCall(String clientId,
                                    VoiceCloneProvider provider,
                                    String externalVoiceId,
                                    String renderedScript,
                                    String language) {
        if (provider == null) {
            throw new ServiceException("Voice cloning provider is required for synthesis", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(externalVoiceId)) {
            throw new ServiceException("Cloned voice id is required for synthesis", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(renderedScript)) {
            throw new ServiceException("Rendered script is required for synthesis", HttpStatus.BAD_REQUEST);
        }

        String resolvedLanguage = normalizeLanguage(language);
        String cacheKey = buildCacheKey(provider, externalVoiceId, resolvedLanguage, renderedScript);

        String cachedKey = audioCacheRepository.findByCacheKey(cacheKey)
                .map(VishingAudioCache::getS3Key)
                .filter(StringUtils::hasText)
                .orElse(null);
        if (cachedKey != null) {
            log.debug("Reusing cached vishing audio, provider={}, cacheKey={}", provider, cacheKey);
            return cachedKey;
        }

        ResolvedProviderCredentials credentials = providerCredentialResolver.resolve(clientId, provider.name());
        VoiceCloneAdapter adapter;
        try {
            adapter = voiceCloneAdapterFactory.getAdapter(provider);
        } catch (IllegalArgumentException ex) {
            throw new ServiceException(ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
        }

        byte[] audioBytes = adapter.synthesize(externalVoiceId, renderedScript, resolvedLanguage, credentials);
        if (audioBytes == null || audioBytes.length == 0) {
            throw new ServiceException("Voice provider returned empty audio", HttpStatus.BAD_GATEWAY);
        }

        String s3Key = deepfakeS3Service.uploadBytes(audioBytes, AUDIO_KEY_PREFIX, AUDIO_EXTENSION, AUDIO_CONTENT_TYPE);
        persistCacheEntry(cacheKey, provider, externalVoiceId, resolvedLanguage, s3Key);
        return s3Key;
    }

    private void persistCacheEntry(String cacheKey, VoiceCloneProvider provider,
                                   String externalVoiceId, String language, String s3Key) {
        try {
            audioCacheRepository.save(VishingAudioCache.builder()
                    .cacheKey(cacheKey)
                    .provider(provider)
                    .externalVoiceId(externalVoiceId)
                    .language(language)
                    .s3Key(s3Key)
                    .build());
        } catch (DuplicateKeyException e) {
            // Concurrent synthesis produced the same key first; the existing asset is equivalent.
            log.debug("Vishing audio cache entry already exists for cacheKey={}", cacheKey);
        }
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return DEFAULT_LANGUAGE;
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private String buildCacheKey(VoiceCloneProvider provider, String externalVoiceId,
                                 String language, String text) {
        String raw = provider.name() + "|" + externalVoiceId + "|" + language + "|" + text;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("Unable to compute audio cache key", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
