package com.aspire.asat.phishing.media.model;

import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Decrypted, ready-to-use credentials for a single third-party provider call.
 *
 * <p>Produced by the provider-credential resolver from either a per-client DB
 * record or a configured YAML fallback, and passed into the thin HTTP clients so
 * they never read secrets from configuration directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolvedProviderCredentials {

    private String providerName;
    private String apiKey;
    private String apiSecret;

    /** Optional base URL override; when blank the client uses its configured default. */
    private String baseUrl;

    /** Optional provider-specific model name; when blank the client uses its configured default. */
    private String modelName;

    /**
     * Category from the DB credential when resolved from {@code provider_credentials};
     * null for YAML fallbacks.
     */
    private ProviderCategory category;

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean hasBaseUrl() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    public boolean hasModelName() {
        return modelName != null && !modelName.isBlank();
    }
}
