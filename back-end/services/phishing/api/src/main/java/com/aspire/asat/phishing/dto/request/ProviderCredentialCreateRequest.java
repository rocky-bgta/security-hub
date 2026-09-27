package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a per-client third-party provider credential.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCredentialCreateRequest {

    @NotBlank(message = "Provider name is required")
    @Size(max = 100, message = "Provider name cannot exceed 100 characters")
    private String providerName;

    @NotNull(message = "Provider category is required")
    private ProviderCategory category;

    @Size(max = 200, message = "Model name cannot exceed 200 characters")
    private String modelName;

    @NotBlank(message = "API key is required")
    @Size(max = 2000, message = "API key cannot exceed 2000 characters")
    private String apiKey;

    @Size(max = 2000, message = "API secret cannot exceed 2000 characters")
    private String apiSecret;

    @Size(max = 500, message = "Base URL cannot exceed 500 characters")
    private String baseUrl;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isDefault = false;
}
