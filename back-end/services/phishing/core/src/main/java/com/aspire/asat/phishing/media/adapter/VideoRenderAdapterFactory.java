package com.aspire.asat.phishing.media.adapter;

import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory/registry for video render adapters.
 */
@Component
@RequiredArgsConstructor
public class VideoRenderAdapterFactory {

    private final List<VideoRenderAdapter> adapters;

    public VideoRenderAdapter getAdapter(VideoRenderProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("VideoRenderProvider is required");
        }
        Map<VideoRenderProvider, VideoRenderAdapter> adapterMap = new EnumMap<>(VideoRenderProvider.class);
        VideoRenderAdapter fallback = null;
        for (VideoRenderAdapter adapter : adapters) {
            if (adapter.isFallback()) {
                fallback = adapter;
            } else if (adapter.getProvider() != null) {
                adapterMap.put(adapter.getProvider(), adapter);
            }
        }
        VideoRenderAdapter adapter = adapterMap.getOrDefault(provider, fallback);
        if (adapter == null) {
            throw new IllegalArgumentException("Video render adapter not implemented for: " + provider);
        }
        return adapter;
    }

    /**
     * Returns providers that have a dedicated (non-fallback) adapter registered.
     */
    public List<VideoRenderProvider> listImplementedProviders() {
        return adapters.stream()
                .filter(adapter -> !adapter.isFallback() && adapter.getProvider() != null)
                .map(VideoRenderAdapter::getProvider)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }
}
