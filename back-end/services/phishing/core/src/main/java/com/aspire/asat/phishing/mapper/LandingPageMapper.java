package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.request.LandingPageUpdateRequest;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.dto.response.LandingPagePreviewDto;
import com.aspire.asat.phishing.model.LandingPage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Mapper for converting LandingPage entities to DTOs.
 * Based on Task-04 Landing Page Library
 */
@Component
public class LandingPageMapper {
    
    /**
     * Convert LandingPage entity to LandingPageDto for list endpoints.
     */
    public LandingPageDto toListItemDto(LandingPage landingPage, boolean canEdit, boolean canDelete) {
        return mapCommonFields(LandingPageDto.builder(), landingPage)
                .landingPageBodyPreview("")
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }

    /**
     * Convert LandingPage entity to LandingPageDto for get-by-id and mutations.
     */
    public LandingPageDto toDto(LandingPage landingPage, boolean canEdit, boolean canDelete) {
        return mapCommonFields(LandingPageDto.builder(), landingPage)
                .landingPageBodyPreview(landingPage.getHtmlContent())
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }

    private LandingPageDto.LandingPageDtoBuilder mapCommonFields(
            LandingPageDto.LandingPageDtoBuilder builder, LandingPage landingPage) {
        return builder
                .pageId(landingPage.getId())
                .name(landingPage.getName())
                .description(landingPage.getDescription())
                .pageType(landingPage.getPageType())
                .category(landingPage.getCategory())
                .difficultyLevel(landingPage.getDifficultyLevel())
                .department(landingPage.getDepartment())
                .targetDepartment(landingPage.getTargetDepartment())
                .targetIndustry(landingPage.getTargetIndustry())
                .constraints(landingPage.getConstraints())
                .urgencyLevel(landingPage.getUrgencyLevel())
                .emotionalTrigger(landingPage.getEmotionalTrigger())
                .dataCaptureType(landingPage.getDataCaptureType())
                .status(landingPage.getStatus() != null ? landingPage.getStatus() : LandingPageStatus.ACTIVE)
                .thumbnailUrl(landingPage.getThumbnailUrl())
                .websiteUrl(landingPage.getWebsiteUrl())
                .tags(landingPage.getTags())
                .captureSubmittedData(landingPage.isCaptureSubmittedData())
                .captureFields(landingPage.getCaptureFields())
                .redirectUrl(landingPage.getRedirectUrl())
                .trackingDomainId(landingPage.getTrackingDomainId())
                .popularity(landingPage.getPopularity())
                .isGlobal(landingPage.isGlobal())
                .isPremium(landingPage.isPremium())
                .createdByRole(landingPage.getCreatedByRole())
                .createdAt(landingPage.getCreatedAt())
                .updatedAt(landingPage.getUpdatedAt());
    }
    
    /**
     * Convert LandingPage entity to LandingPagePreviewDto
     * @param landingPage Entity to convert
     * @return LandingPagePreviewDto with full HTML content
     */
    public LandingPagePreviewDto toPreviewDto(LandingPage landingPage) {
        return LandingPagePreviewDto.builder()
                .pageId(landingPage.getId())
                .name(landingPage.getName())
                .description(landingPage.getDescription())
                .pageType(landingPage.getPageType())
                .category(landingPage.getCategory())
                .difficultyLevel(landingPage.getDifficultyLevel())
                .status(landingPage.getStatus() != null ? landingPage.getStatus() : LandingPageStatus.ACTIVE)
                .thumbnailUrl(landingPage.getThumbnailUrl())
                .websiteUrl(landingPage.getWebsiteUrl())
                .tags(landingPage.getTags() != null ? new ArrayList<>(landingPage.getTags()) : new ArrayList<>())
                .popularity(landingPage.getPopularity())
                .isPremium(landingPage.isPremium())
                .templateGenerationType(landingPage.getTemplateGenerationType())
                .htmlContent(landingPage.getHtmlContent())
                .captureSubmittedData(landingPage.isCaptureSubmittedData())
                .captureFields(landingPage.getCaptureFields() != null ? new ArrayList<>(landingPage.getCaptureFields()) : new ArrayList<>())
                .redirectUrl(landingPage.getRedirectUrl())
                .trackingDomainId(landingPage.getTrackingDomainId())
                .build();
    }
    
    /**
     * Update LandingPage entity from update request
     * @param landingPage Entity to update
     * @param request Update request data
     */
    public void updateFromRequest(LandingPage landingPage, LandingPageUpdateRequest request) {
        landingPage.setName(request.getName());
        landingPage.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            landingPage.setCategory(request.getCategory());
        }
        if (request.getDifficultyLevel() != null) {
            landingPage.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getDepartment() != null) {
            landingPage.setDepartment(request.getDepartment());
        }
        if (request.getTargetDepartment() != null) {
            landingPage.setTargetDepartment(request.getTargetDepartment());
        }
        if (request.getTargetIndustry() != null) {
            landingPage.setTargetIndustry(request.getTargetIndustry());
        }
        if (request.getConstraints() != null) {
            landingPage.setConstraints(request.getConstraints());
        }
        if (request.getUrgencyLevel() != null) {
            landingPage.setUrgencyLevel(request.getUrgencyLevel());
        }
        if (request.getEmotionalTrigger() != null) {
            landingPage.setEmotionalTrigger(request.getEmotionalTrigger());
        }
        if (request.getDataCaptureType() != null) {
            landingPage.setDataCaptureType(request.getDataCaptureType());
        }
        if (request.getStatus() != null) {
            landingPage.setStatus(request.getStatus());
        }
        
        if (request.getHtmlContent() != null) {
            landingPage.setHtmlContent(request.getHtmlContent());
        }

        if (request.getThumbnailUrl() != null) {
            landingPage.setThumbnailUrl(request.getThumbnailUrl());
        }
        
        if (request.getTags() != null) {
            landingPage.setTags(new ArrayList<>(request.getTags()));
        }
        
        landingPage.setCaptureSubmittedData(request.isCaptureSubmittedData());
        
        if (request.getCaptureFields() != null) {
            landingPage.setCaptureFields(new ArrayList<>(request.getCaptureFields()));
        }
        
        landingPage.setRedirectUrl(request.getRedirectUrl());
        landingPage.setTrackingDomainId(request.getTrackingDomainId());
    }
    
    /**
     * Create a duplicate of a landing page with new ownership
     * @param original Original landing page
     * @param clientId New client ID
     * @param userId New creator user ID
     * @param userRole New creator role
     * @return Duplicated LandingPage entity (unsaved)
     */
    public LandingPage createDuplicate(LandingPage original, String clientId, String userId, String userRole) {
        return LandingPage.builder()
                .clientId(clientId)
                .name(original.getName() + " (Copy)")
                .description(original.getDescription())
                .pageType(original.getPageType())
                .category(original.getCategory())
                .difficultyLevel(original.getDifficultyLevel())
                .department(original.getDepartment())
                .targetDepartment(original.getTargetDepartment())
                .targetIndustry(original.getTargetIndustry())
                .constraints(original.getConstraints())
                .urgencyLevel(original.getUrgencyLevel())
                .emotionalTrigger(original.getEmotionalTrigger())
                .dataCaptureType(original.getDataCaptureType())
                .status(original.getStatus() != null ? original.getStatus() : LandingPageStatus.ACTIVE)
                .htmlContent(original.getHtmlContent())
                .thumbnailUrl(original.getThumbnailUrl())
                .websiteUrl(original.getWebsiteUrl())
                .tags(original.getTags() != null ? new ArrayList<>(original.getTags()) : new ArrayList<>())
                .captureSubmittedData(original.isCaptureSubmittedData())
                .captureFields(original.getCaptureFields() != null 
                        ? new ArrayList<>(original.getCaptureFields()) : new ArrayList<>())
                .redirectUrl(original.getRedirectUrl())
                .trackingDomainId(original.getTrackingDomainId())
                .popularity(0) // Reset popularity for duplicate
                .isGlobal(false) // Duplicates are not global by default
                .isPremium(false)
                .templateGenerationType(original.getTemplateGenerationType())
                .createdBy(userId)
                .createdByRole(userRole)
                .build();
    }
}
