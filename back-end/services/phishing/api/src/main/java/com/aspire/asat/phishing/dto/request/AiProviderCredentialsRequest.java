package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider credentials supplied by the client.
 * Secrets should never be logged and must be stored only as SecureString in AWS SSM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProviderCredentialsRequest {

    /**
     * Optional friendly name for this secret (stored as metadata in DB).
     */
    @Size(max = 100)
    private String secretName;

    /**
     * Optional description for this secret (stored as metadata in DB).
     */
    @Size(max = 500)
    private String description;

    /**
     * Provider API key.
     * For OPENAI this is typically required; for other providers it may vary.
     */
    @Size(min = 1, max = 5000)
    private String apiKey;

    /**
     * Provider API secret (optional).
     */
    @Size(min = 0, max = 5000)
    private String apiSecret;

}

