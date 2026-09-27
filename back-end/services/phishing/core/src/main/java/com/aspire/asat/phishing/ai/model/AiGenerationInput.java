package com.aspire.asat.phishing.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider-agnostic normalized input for HTML page fix generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationInput {

    private String prompt;
    private String elementHtml;
    private String templateCode;
}
