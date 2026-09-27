package com.aspire.asat.phishing.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider-neutral generated payload for phishing email content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGeneratedEmailContent {

    private String htmlBody;
    private String textBody;
    private String suggestedDifficulty;
    private boolean success;
    private String errorMessage;
}
