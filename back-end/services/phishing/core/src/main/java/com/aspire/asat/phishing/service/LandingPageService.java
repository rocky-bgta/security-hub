package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.request.LandingPageCreateRequest;
import com.aspire.asat.phishing.dto.request.LandingPageUpdateRequest;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.dto.response.LandingPagePreviewDto;
import com.aspire.asat.phishing.dto.response.ValidationResult;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for landing page management.
 */
public interface LandingPageService {

    List<LandingPageDto> getLandingPages(
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            String clientId,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder);

    long countLandingPages(
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            String clientId);

    Optional<LandingPageDto> getLandingPageById(String pageId);

    Optional<LandingPagePreviewDto> getLandingPagePreview(String pageId);

    LandingPageDto updateLandingPage(String pageId, LandingPageUpdateRequest request);

    void deleteLandingPage(String pageId);

    LandingPageDto duplicateLandingPage(String pageId);

    void incrementPopularity(String pageId);

    LandingPageDto createLandingPage(LandingPageCreateRequest request);

    ValidationResult validateHtml(String htmlContent, boolean validateForms, boolean securityCheck);

    boolean isPageNameExists(String name);

    String uploadThumbnail(String pageId, String thumbnailUrl);

    /**
     * Get landing pages bound to the given email or SMS template.
     */
    List<LandingPageDto> getBoundLandingPages(String templateId);

    /**
     * Get email/SMS templates bound to the given landing page.
     */
    List<EmailTemplateDto> getBoundEmailTemplates(String pageId);
}
