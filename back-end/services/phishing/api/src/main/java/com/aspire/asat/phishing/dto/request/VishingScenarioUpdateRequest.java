package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.LlmEscalationLimit;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingScenarioUpdateRequest {

    @NotBlank(message = "Scenario name is required")
    @Size(max = 150)
    private String scenarioName;

    private String description;

    private String attackTemplateId;

    private String attackTemplateName;

    private String language;

    private String tone;

    private String role;

    @NotBlank(message = "Script body is required")
    private String scriptBody;

    @Builder.Default
    private boolean enableLlmResponses = true;

    @Builder.Default
    private VishingInteractionMode interactionMode = VishingInteractionMode.BOTH;

    private LlmEscalationLimit escalationLimit;
}
