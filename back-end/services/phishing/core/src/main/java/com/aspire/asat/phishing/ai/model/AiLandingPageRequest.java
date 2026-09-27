package com.aspire.asat.phishing.ai.model;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Normalized request for AI-generated landing pages.
 * Structural/page fields are on this object; prompt styling/content is in {@link #options}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiLandingPageRequest {

    private AiProviderType providerType;
    private String model;
    private String apiKey;
    private String apiSecret;

    private String pageName;
    private String pageType;
    private String category;
    private String targetDepartment;
    private String urgencyLevel;
    private String emotionalTrigger;
    private String voiceInput;
    private String additionalContext;
    private AiGenerationOptions options;
}
