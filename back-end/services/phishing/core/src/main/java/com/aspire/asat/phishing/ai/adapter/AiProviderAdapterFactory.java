package com.aspire.asat.phishing.ai.adapter;

import com.aspire.asat.phishing.ai.adapter.AiProviderAdapter;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory/registry for provider adapters.
 */
@Component
@RequiredArgsConstructor
public class AiProviderAdapterFactory {

    private final List<AiProviderAdapter> adapters;

    /**
     * Resolve adapter for a given provider.
     * @throws IllegalArgumentException if no adapter is registered.
     */
    public AiProviderAdapter getAdapter(AiProviderType type) {
        if (type == null) {
            throw new IllegalArgumentException("AiProviderType is required");
        }

        Map<AiProviderType, AiProviderAdapter> adapterMap = new EnumMap<>(AiProviderType.class);
        for (AiProviderAdapter adapter : adapters) {
            adapterMap.put(adapter.getProviderType(), adapter);
        }

        AiProviderAdapter adapter = adapterMap.get(type);
        if (adapter == null) {
            throw new IllegalArgumentException("AI provider adapter not implemented for: " + type);
        }
        return adapter;
    }
}

