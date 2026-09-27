package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Per-client third-party provider credentials used by the deepfake pipeline
 * (voice cloning + video rendering). Secrets are stored encrypted at rest via
 * {@code CredentialEncryptionService}; they are never returned to API consumers.
 *
 * <p>A client may configure at most one credential per {@code providerName}
 * (enforced by the unique compound index) and exactly one default per
 * {@link ProviderCategory}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "provider_credentials")
@CompoundIndexes({
        @CompoundIndex(name = "client_provider_unique",
                def = "{'clientId': 1, 'providerName': 1}", unique = true)
})
public class ProviderCredential {

    @Id
    private String id;

    @Indexed
    private String clientId;

    /**
     * Provider identifier, e.g. {@code ELEVENLABS}, {@code FISH_AUDIO}, {@code HEYGEN}.
     * Stored as free text so new providers can be added without code changes.
     */
    private String providerName;

    private ProviderCategory category;

    /**
     * Optional provider-specific model name, e.g. {@code eleven_multilingual_v2}
     * or {@code s2-pro}. When blank the client falls back to its configured default.
     */
    private String modelName;

    /** Encrypted (ENC:) API key. */
    private String apiKey;

    /** Encrypted (ENC:) API secret; optional for providers that only need a key. */
    private String apiSecret;

    /** Optional base URL override; falls back to the client's configured default when blank. */
    private String baseUrl;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isDefault = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
