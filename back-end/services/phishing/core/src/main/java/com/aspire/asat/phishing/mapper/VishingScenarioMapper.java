package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.request.VishingScenarioCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingScenarioUpdateRequest;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import com.aspire.asat.phishing.dto.response.VishingScenarioDto;
import com.aspire.asat.phishing.model.VishingScenario;
import com.aspire.asat.phishing.util.VishingScriptPlaceholderUtils;
import org.springframework.stereotype.Component;

@Component
public class VishingScenarioMapper {

    public VishingScenarioDto toDto(VishingScenario entity) {
        if (entity == null) {
            return null;
        }
        return VishingScenarioDto.builder()
                .id(entity.getId())
                .scenarioName(entity.getScenarioName())
                .description(entity.getDescription())
                .attackTemplateId(entity.getAttackTemplateId())
                .attackTemplateName(entity.getAttackTemplateName())
                .language(entity.getLanguage())
                .tone(entity.getTone())
                .role(entity.getRole())
                .scriptBody(entity.getScriptBody())
                .detectedVariables(entity.getDetectedVariables())
                .status(entity.getStatus())
                .isGlobal(entity.isGlobal())
                .enableLlmResponses(entity.isEnableLlmResponses())
                .interactionMode(entity.getInteractionMode())
                .escalationLimit(entity.getEscalationLimit())
                .popularity(entity.getPopularity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public VishingScenario toEntity(VishingScenarioCreateRequest request, String clientId) {
        return VishingScenario.builder()
                .clientId(clientId)
                .scenarioName(request.getScenarioName().trim())
                .description(request.getDescription())
                .attackTemplateId(request.getAttackTemplateId())
                .attackTemplateName(request.getAttackTemplateName())
                .language(request.getLanguage())
                .tone(request.getTone())
                .role(request.getRole())
                .scriptBody(request.getScriptBody())
                .detectedVariables(VishingScriptPlaceholderUtils.detectVariables(request.getScriptBody()))
                .enableLlmResponses(request.isEnableLlmResponses())
                .interactionMode(resolveInteractionMode(request.getInteractionMode()))
                .escalationLimit(request.getEscalationLimit())
                .build();
    }

    public void applyUpdate(VishingScenario entity, VishingScenarioUpdateRequest request) {
        entity.setScenarioName(request.getScenarioName().trim());
        entity.setDescription(request.getDescription());
        entity.setAttackTemplateId(request.getAttackTemplateId());
        entity.setAttackTemplateName(request.getAttackTemplateName());
        entity.setLanguage(request.getLanguage());
        entity.setTone(request.getTone());
        entity.setRole(request.getRole());
        entity.setScriptBody(request.getScriptBody());
        entity.setDetectedVariables(VishingScriptPlaceholderUtils.detectVariables(request.getScriptBody()));
        entity.setEnableLlmResponses(request.isEnableLlmResponses());
        entity.setInteractionMode(resolveInteractionMode(request.getInteractionMode()));
        entity.setEscalationLimit(request.getEscalationLimit());
    }

    private VishingInteractionMode resolveInteractionMode(VishingInteractionMode mode) {
        return mode != null ? mode : VishingInteractionMode.BOTH;
    }
}
