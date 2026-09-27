package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.request.EmailTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.EmailTemplatePreviewDto;
import com.aspire.asat.phishing.model.EmailTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Mapper for converting between EmailTemplate entity and DTOs.
 */
@Component
public class EmailTemplateMapper {
    
    /**
     * Convert EmailTemplate entity to EmailTemplateDto for list endpoints.
     */
    public EmailTemplateDto toListItemDto(EmailTemplate template, boolean canEdit, boolean canDelete) {
        if (template == null) {
            return null;
        }
        return mapCommonFields(EmailTemplateDto.builder(), template)
                .emailBodyPreview("")
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }

    /**
     * Convert EmailTemplate entity to EmailTemplateDto (full body) for get-by-id and mutations.
     */
    public EmailTemplateDto toDto(EmailTemplate template, boolean canEdit, boolean canDelete) {
        if (template == null) {
            return null;
        }
        
        return mapCommonFields(EmailTemplateDto.builder(), template)
                .emailBodyPreview(template.getEmailBody())
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }
    
    /**
     * Convert EmailTemplate entity to EmailTemplatePreviewDto
     * @param template The template entity
     * @return EmailTemplatePreviewDto with full content
     */
    public EmailTemplatePreviewDto toPreviewDto(EmailTemplate template) {
        if (template == null) {
            return null;
        }
        
        return EmailTemplatePreviewDto.builder()
                .templateId(template.getId())
                .templateName(template.getTemplateName())
                .description(template.getDescription())
                .emailType(template.getEmailType())
                .templateType(template.getTemplateType())
                .payloadType(template.getPayloadType())
                .difficultyLevel(template.getDifficultyLevel())
                .serviceLocation(template.getServiceLocation())
                .tags(template.getTags() != null ? new ArrayList<>(template.getTags()) : new ArrayList<>())
                .language(template.getLanguage())
                .popularity(template.getPopularity())
                .isPremium(template.isPremium())
                .templateGenerationType(template.getTemplateGenerationType())
                .status(template.getStatus() != null ? template.getStatus() : EmailTemplateStatus.ACTIVE)
                .emailSubject(template.getEmailSubject())
                .thumbnailUrl(template.getThumbnailUrl())
                .emailBody(template.getEmailBody())
                .smsBody(template.getSmsBody())
                .emailBodyText(template.getEmailBodyText())
                .attachments(template.getAttachments() != null ? new ArrayList<>(template.getAttachments()) : new ArrayList<>())
                .build();
    }
    
    /**
     * Update EmailTemplate entity from update request
     * @param template The template entity to update
     * @param request The update request
     */
    public void updateFromRequest(EmailTemplate template, EmailTemplateUpdateRequest request) {
        if (request.getTemplateName() != null) {
            template.setTemplateName(request.getTemplateName());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getEmailSubject() != null) {
            template.setEmailSubject(request.getEmailSubject());
        }
        if (request.getEmailBody() != null) {
            template.setEmailBody(request.getEmailBody());
        }
        if (request.getEmailBodyText() != null) {
            template.setEmailBodyText(request.getEmailBodyText());
        }
        if (request.getSmsBody() != null) {
            template.setSmsBody(request.getSmsBody());
        }
        if (request.getTemplateType() != null) {
            template.setTemplateType(request.getTemplateType());
        }
        if (request.getPayloadType() != null) {
            template.setPayloadType(request.getPayloadType());
        }
        if (request.getDifficultyLevel() != null) {
            template.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTone() != null) {
            template.setTone(request.getTone());
        }
        if (request.getDepartment() != null) {
            template.setDepartment(request.getDepartment());
        }
        if (request.getTargetIndustry() != null) {
            template.setTargetIndustry(request.getTargetIndustry());
        }
        if (request.getConstraints() != null) {
            template.setConstraints(request.getConstraints());
        }
        if (request.getExpectedUserAction() != null) {
            template.setExpectedUserAction(request.getExpectedUserAction());
        }
        if (request.getTriggerEvent() != null) {
            template.setTriggerEvent(request.getTriggerEvent());
        }
        if (request.getUrgencyLevel() != null) {
            template.setUrgencyLevel(request.getUrgencyLevel());
        }
        if (request.getSocialEngineeringStrategy() != null) {
            template.setSocialEngineeringStrategy(request.getSocialEngineeringStrategy());
        }
        if (request.getEmotionalTrigger() != null) {
            template.setEmotionalTrigger(request.getEmotionalTrigger());
        }
        if (request.getCampaignObjective() != null) {
            template.setCampaignObjective(request.getCampaignObjective());
        }
        if (request.getAttackerPersona() != null) {
            template.setAttackerPersona(request.getAttackerPersona());
        }
        if (request.getAttackTechnique() != null) {
            template.setAttackTechnique(request.getAttackTechnique());
        }
        if (request.getBrand() != null) {
            template.setBrand(request.getBrand());
        }
        if (request.getCallToAction() != null) {
            template.setCallToAction(request.getCallToAction());
        }
        if (request.getServiceLocation() != null) {
            template.setServiceLocation(request.getServiceLocation());
        }
        if (request.getThumbnailUrl() != null) {
            template.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getTags() != null) {
            template.setTags(request.getTags());
        }
        if (request.getEmployeeDataRequired() != null) {
            template.setEmployeeDataRequired(request.getEmployeeDataRequired());
        }
        if (request.getLanguage() != null) {
            template.setLanguage(request.getLanguage());
        }
        if (request.getStatus() != null) {
            template.setStatus(request.getStatus());
        }
    }
    
    /**
     * Create a duplicate copy of a template
     * @param original The original template
     * @param clientId The client ID for the new copy
     * @param createdBy The user creating the copy
     * @param createdByRole The role of the user
     * @return New EmailTemplate with "(Copy)" suffix
     */
    public EmailTemplate createDuplicate(EmailTemplate original, String clientId, 
                                          String createdBy, String createdByRole) {
        return EmailTemplate.builder()
                .clientId(clientId)
                .templateName(original.getTemplateName() + " (Copy)")
                .description(original.getDescription())
                .organizationName(original.getOrganizationName())
                .organizationDomain(original.getOrganizationDomain())
                .emailSubject(original.getEmailSubject())
                .emailBody(original.getEmailBody())
                .emailBodyText(original.getEmailBodyText())
                .templateType(original.getTemplateType())
                .smsBody(original.getSmsBody())
                .emailType(original.getEmailType())
                .payloadType(original.getPayloadType())
                .difficultyLevel(original.getDifficultyLevel())
                .tone(original.getTone())
                .department(original.getDepartment())
                .targetIndustry(original.getTargetIndustry())
                .constraints(original.getConstraints())
                .expectedUserAction(original.getExpectedUserAction())
                .triggerEvent(original.getTriggerEvent())
                .urgencyLevel(original.getUrgencyLevel())
                .socialEngineeringStrategy(original.getSocialEngineeringStrategy())
                .emotionalTrigger(original.getEmotionalTrigger())
                .campaignObjective(original.getCampaignObjective())
                .attackerPersona(original.getAttackerPersona())
                .attackTechnique(original.getAttackTechnique())
                .brand(original.getBrand())
                .callToAction(original.getCallToAction())
                .serviceLocation(original.getServiceLocation())
                .tags(original.getTags() != null ? new ArrayList<>(original.getTags()) : null)
                .employeeDataRequired(original.getEmployeeDataRequired() != null 
                        ? new ArrayList<>(original.getEmployeeDataRequired()) : null)
                .thumbnailUrl(original.getThumbnailUrl())
                .attachments(original.getAttachments() != null 
                        ? new ArrayList<>(original.getAttachments()) : new ArrayList<>())
                .language(original.getLanguage())
                .popularity(0)
                .status(original.getStatus() != null ? original.getStatus() : EmailTemplateStatus.ACTIVE)
                .isGlobal(false)  // Duplicates are never global
                .isPremium(false) // Duplicates are never premium
                .templateGenerationType(original.getTemplateGenerationType())
                .createdBy(createdBy)
                .createdByRole(createdByRole)
                .build();
    }

    private EmailTemplateDto.EmailTemplateDtoBuilder mapCommonFields(
            EmailTemplateDto.EmailTemplateDtoBuilder builder, EmailTemplate template) {
        return builder
                .templateId(template.getId())
                .templateName(template.getTemplateName())
                .description(template.getDescription())
                .emailSubject(template.getEmailSubject())
                .emailType(template.getEmailType())
                .templateType(template.getTemplateType())
                .smsBody(template.getSmsBody())
                .status(template.getStatus() != null ? template.getStatus() : EmailTemplateStatus.ACTIVE)
                .payloadType(template.getPayloadType())
                .difficultyLevel(template.getDifficultyLevel())
                .tone(template.getTone())
                .department(template.getDepartment())
                .targetIndustry(template.getTargetIndustry())
                .constraints(template.getConstraints())
                .expectedUserAction(template.getExpectedUserAction())
                .triggerEvent(template.getTriggerEvent())
                .urgencyLevel(template.getUrgencyLevel())
                .socialEngineeringStrategy(template.getSocialEngineeringStrategy())
                .emotionalTrigger(template.getEmotionalTrigger())
                .campaignObjective(template.getCampaignObjective())
                .attackerPersona(template.getAttackerPersona())
                .attackTechnique(template.getAttackTechnique())
                .brand(template.getBrand())
                .callToAction(template.getCallToAction())
                .serviceLocation(template.getServiceLocation())
                .tags(template.getTags())
                .employeeDataRequired(template.getEmployeeDataRequired())
                .thumbnailUrl(template.getThumbnailUrl())
                .attachments(template.getAttachments() != null ? new ArrayList<>(template.getAttachments()) : new ArrayList<>())
                .language(template.getLanguage())
                .popularity(template.getPopularity())
                .isGlobal(template.isGlobal())
                .isPremium(template.isPremium())
                .templateGenerationType(template.getTemplateGenerationType())
                .createdByRole(template.getCreatedByRole())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .landingPageIds(template.getLandingPageIds() != null
                        ? new ArrayList<>(template.getLandingPageIds()) : new ArrayList<>());
    }
}
