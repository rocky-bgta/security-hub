package com.aspire.asat.phishing.media.adapter;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Factory/registry for voice cloning adapters.
 */
@Component
@RequiredArgsConstructor
public class VoiceCloneAdapterFactory {

    private static final String ELEVENLABS_HOST = "elevenlabs.io";
    private static final String FISH_AUDIO_HOST = "fish.audio";

    private final List<VoiceCloneAdapter> adapters;

    public VoiceCloneAdapter getAdapter(VoiceCloneProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("VoiceCloneProvider is required");
        }
        Map<VoiceCloneProvider, VoiceCloneAdapter> adapterMap = new EnumMap<>(VoiceCloneProvider.class);
        for (VoiceCloneAdapter adapter : adapters) {
            adapterMap.put(adapter.getProvider(), adapter);
        }
        VoiceCloneAdapter adapter = adapterMap.get(provider);
        if (adapter == null) {
            throw new IllegalArgumentException("Voice clone adapter not implemented for: " + provider);
        }
        return adapter;
    }

    public VoiceCloneAdapter getAdapterForBaseUrl(String baseUrl) {
        return getAdapter(resolveProviderFromBaseUrl(baseUrl));
    }

    /**
     * Selects the implementation from the credential base URL host.
     * Provider display names are free text and are not used here.
     */
    public static VoiceCloneProvider resolveProviderFromBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new ServiceException(
                    "Base URL is required to select the voice provider", HttpStatus.BAD_REQUEST);
        }
        String host = extractHost(baseUrl.trim());
        if (host.contains(ELEVENLABS_HOST)) {
            return VoiceCloneProvider.ELEVENLABS;
        }
        if (host.contains(FISH_AUDIO_HOST)) {
            return VoiceCloneProvider.FISH_AUDIO;
        }
        throw new ServiceException(
                "Base URL must be an ElevenLabs or Fish Audio API host", HttpStatus.BAD_REQUEST);
    }

    private static String extractHost(String baseUrl) {
        String candidate = baseUrl;
        if (!candidate.contains("://")) {
            candidate = "https://" + candidate;
        }
        try {
            URI uri = URI.create(candidate);
            String host = uri.getHost();
            if (host != null && !host.isBlank()) {
                return host.toLowerCase(Locale.ROOT);
            }
        } catch (IllegalArgumentException ignored) {
            // fall through to raw comparison
        }
        return baseUrl.toLowerCase(Locale.ROOT);
    }
}
