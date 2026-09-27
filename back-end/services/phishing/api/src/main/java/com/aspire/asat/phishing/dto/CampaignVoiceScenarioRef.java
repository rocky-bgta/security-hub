package com.aspire.asat.phishing.dto;

import com.aspire.asat.phishing.dto.enums.LlmEscalationLimit;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignVoiceScenarioRef {

    private String scenarioId;
    private String scenarioName;
    private String attackTemplateId;
    private String attackTemplateName;
    private String language;
    private String tone;
    private String scriptBody;
    @Builder.Default
    private List<String> detectedVariables = new ArrayList<>();
    private boolean enableLlmResponses;
    @Builder.Default
    private VishingInteractionMode interactionMode = VishingInteractionMode.BOTH;
    private LlmEscalationLimit escalationLimit;
}
