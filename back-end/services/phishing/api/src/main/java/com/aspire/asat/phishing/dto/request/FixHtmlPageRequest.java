package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for fixing an HTML page with AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixHtmlPageRequest {

    @NotNull(message = "providerType is required")
    private AiProviderType providerType;

    /**
     * Optional provider model. If omitted, adapter default model is used.
     */
    private String model;

    @NotNull(message = "input is required")
    @Valid
    private AiGenerationInputRequest input;
}
