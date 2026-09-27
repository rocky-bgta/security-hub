package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.request.VishingAttackTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingAttackTemplateDto;
import com.aspire.asat.phishing.model.VishingAttackTemplate;
import com.aspire.asat.phishing.util.VishingScriptPlaceholderUtils;
import org.springframework.stereotype.Component;

/**
 * Maps between vishing attack template entities and DTOs.
 */
@Component
public class VishingAttackTemplateMapper {

    public VishingAttackTemplateDto toDto(VishingAttackTemplate entity) {
        if (entity == null) {
            return null;
        }
        return VishingAttackTemplateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .script(entity.getScript())
                .variables(entity.getVariables())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public VishingAttackTemplate toEntity(VishingAttackTemplateCreateRequest request, String createdBy) {
        String script = request.getScript();
        return VishingAttackTemplate.builder()
                .name(request.getName().trim())
                .script(script)
                .variables(VishingScriptPlaceholderUtils.detectVariables(script))
                .createdBy(createdBy)
                .build();
    }

    public void applyUpdate(VishingAttackTemplate entity, VishingAttackTemplateUpdateRequest request) {
        String script = request.getScript();
        entity.setName(request.getName().trim());
        entity.setScript(script);
        entity.setVariables(VishingScriptPlaceholderUtils.detectVariables(script));
    }
}
