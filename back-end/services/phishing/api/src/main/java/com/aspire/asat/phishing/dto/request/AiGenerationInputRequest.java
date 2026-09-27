package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input payload for HTML page fix generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationInputRequest {

    @NotBlank(message = "prompt is required")
    private String prompt;

    @NotBlank(message = "elementHtml is required")
    private String elementHtml;

    @NotBlank(message = "templateCode is required")
    private String templateCode;
}
