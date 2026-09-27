package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for fixing an HTML email template with AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixEmailTemplateRequest {

    @NotNull(message = "providerType is required")
    private AiProviderType providerType;

    /**
     * Optional provider model. If omitted, adapter default model is used.
     */
    private String model;

    @NotBlank(message = "prompt is required")
    private String prompt;

    @NotBlank(message = "elementHtml is required")
    private String elementHtml;

    @NotBlank(message = "templateCode is required")
    private String templateCode;
}
