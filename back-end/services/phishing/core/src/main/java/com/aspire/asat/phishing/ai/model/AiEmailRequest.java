package com.aspire.asat.phishing.ai.model;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Normalized request object for AI-generated phishing email templates.
 * Prompt parameters live in {@link #options}; {@link #templateName} and {@link #payloadType} identify the template.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEmailRequest {

    private AiProviderType providerType;
    private String model;
    private String apiKey;
    private String apiSecret;

    private String templateName;
    private String payloadType;
    private AiGenerationOptions options;
}
