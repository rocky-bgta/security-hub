package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persists global AI provider credentials for the current tenant: values are written to AWS SSM
 * and parameter names are stored in {@code ai_provider_secret_refs}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGlobalConfigRequest {

    @NotNull(message = "providerType is required")
    private AiProviderType providerType;

    @NotBlank(message = "apiKey is required")
    @Size(max = 5000)
    private String apiKey;

    @Size(max = 5000)
    private String apiSecret;

    private String description;
}
