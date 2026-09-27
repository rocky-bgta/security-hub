package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.LlmEscalationLimit;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import com.aspire.asat.phishing.dto.enums.VishingScenarioStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingScenarioDto {

    private String id;
    private String scenarioName;
    private String description;
    private String attackTemplateId;
    private String attackTemplateName;
    private String language;
    private String tone;
    private String role;
    private String scriptBody;
    private List<String> detectedVariables;
    private VishingScenarioStatus status;
    private boolean isGlobal;
    private boolean enableLlmResponses;
    private VishingInteractionMode interactionMode;
    private LlmEscalationLimit escalationLimit;
    private int popularity;
    private Instant createdAt;
    private Instant updatedAt;
}
