package com.aspire.asat.phishing.ai.model;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Normalized request for fixing an HTML page using AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFixHtmlPageRequest {

    private AiProviderType providerType;
    private String model;
    private AiGenerationInput input;
}
