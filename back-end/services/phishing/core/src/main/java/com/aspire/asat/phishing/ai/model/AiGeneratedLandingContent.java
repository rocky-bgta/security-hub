package com.aspire.asat.phishing.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider-neutral generated payload for phishing landing page content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGeneratedLandingContent {

    private String htmlContent;
    private String title;
    private String suggestedDifficulty;
    private boolean success;
    private String errorMessage;
}
