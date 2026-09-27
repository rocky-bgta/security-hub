package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Mongo document storing only metadata (parameter names), not raw secrets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_provider_secret_refs")
public class AiProviderSecretRef {

    @Id
    private String id;

    @Indexed
    private String clientAdminId;

    @Indexed
    private AiProviderType providerType;

    @Indexed
    private String secretName;

    private String description;

    /**
     * Full SSM parameter name containing SecureString for API key.
     */
    @Indexed
    private String apiKeyParameterName;

    /**
     * Full SSM parameter name containing SecureString for API secret (optional).
     */
    private String apiSecretParameterName;

    private boolean active;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

