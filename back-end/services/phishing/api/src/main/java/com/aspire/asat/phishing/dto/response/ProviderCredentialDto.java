package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for a per-client provider credential.
 *
 * <p>Secrets are never returned in full: only {@link #apiKeyLast4 the last four
 * characters} of the API key and a {@link #hasApiSecret} flag are exposed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCredentialDto {

    private String id;
    private String providerName;
    private ProviderCategory category;

    /** Optional provider-specific model name (e.g. {@code eleven_multilingual_v2}). */
    private String modelName;

    /** Last four characters of the API key for identification (never the full key). */
    private String apiKeyLast4;

    /** Whether an API secret is configured for this provider. */
    private Boolean hasApiSecret;

    private String baseUrl;
    private Boolean isActive;
    private Boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
}
