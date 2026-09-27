package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.request.AITemplateGenerateRequest;
import com.aspire.asat.phishing.dto.request.EmailTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.EmailTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.EmailTemplatePreviewDto;
import com.aspire.asat.phishing.dto.response.FilterOptionsDto;
import com.aspire.asat.phishing.dto.response.SanitizeHtmlResult;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for email template management.
 * Based on BRD Use Case 2.1.3
 */
public interface EmailTemplateService {
    
    /**
     * Get all templates with pagination, search, and filters
     * @param searchParam Search by name, subject, tags
     * @param difficultyLevel Filter by difficulty
     * @param payloadType Filter by payload type
     * @param location Filter by service location
     * @param tags Filter by tags
     * @param language Filter by language
     * @param offset Page offset
     * @param pageSize Items per page
     * @param sortBy Field to sort by
     * @param sortOrder Sort direction (asc/desc)
     * @return List of EmailTemplateDto
     */
    List<EmailTemplateDto> getTemplates(
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            String clientId,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder);
    
    /**
     * Count templates matching filters.
     * Used by pagination.
     *
     * @param searchParam Search term (templateName/emailSubject/tags)
     * @param difficultyLevel Filter by difficulty
     * @param payloadType Filter by payload type
     * @param location Filter by service location
     * @param tags Filter by tags
     * @param language Filter by language
     * @return Total count
     */
    long countTemplates(
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            String clientId);
    
    /**
     * Get template by ID
     * @param templateId Template ID
     * @return Optional EmailTemplateDto
     */
    Optional<EmailTemplateDto> getTemplateById(String templateId);
    
    /**
     * Get full template preview with HTML body
     * @param templateId Template ID
     * @return Optional EmailTemplatePreviewDto
     */
    Optional<EmailTemplatePreviewDto> getTemplatePreview(String templateId);
    
    /**
     * Update an existing template
     * BR-09: Admin can only edit templates they created
     * BR-10: Super Admin templates cannot be edited by regular Admins
     * @param templateId Template ID
     * @param request Update request
     * @return Updated EmailTemplateDto
     */
    EmailTemplateDto updateTemplate(String templateId, EmailTemplateUpdateRequest request);
    
    /**
     * Delete a template
     * BR-09: Admin can only delete templates they created
     * BR-10: Super Admin templates cannot be deleted by regular Admins
     * @param templateId Template ID
     */
    void deleteTemplate(String templateId);
    
    /**
     * Duplicate a template
     * BR-11: Creates new entity with "(Copy)" suffix
     * @param templateId Template ID to duplicate
     * @return New duplicated EmailTemplateDto
     */
    EmailTemplateDto duplicateTemplate(String templateId);
    
    /**
     * Get available predefined tags
     * BR-08: Tags from predefined list
     * @return List of available tags
     */
    List<String> getAvailableTags();
    
    /**
     * Get all filter options for UI dropdowns
     * @return FilterOptionsDto with all available filter values
     */
    FilterOptionsDto getFilterOptions(PayloadTypeChannel payloadTypeChannel);
    
    /**
     * Increment template popularity (usage count)
     * @param templateId Template ID
     */
    void incrementPopularity(String templateId);
    
    // --- Task-03: Template Creation Methods ---
    
    /**
     * Create a new email template manually
     * BR-01: Admin must fill all required fields before saving
     * BR-02: Email Type is auto-selected as STANDARD_PHISH by default
     * BR-03: At least one Employee Data Required field must be selected
     * BR-09: Template name must be unique per client
     * @param request Template creation request
     * @return Created EmailTemplateDto
     */
    EmailTemplateDto createTemplate(EmailTemplateCreateRequest request);
    
    /**
     * Generate a template using AI
     * BR-07: AI-generated content must pass compliance checks
     * @param request AI generation request with context parameters
     * @return Generated EmailTemplateDto
     */
    EmailTemplateDto generateAITemplate(AITemplateGenerateRequest request);
    
    /**
     * Sanitize HTML content
     * BR-04: Email body HTML must be sanitized to remove script tags and event handlers
     * BR-10: HTML content must be validated for syntax before saving
     * @param htmlContent Raw HTML content
     * @return SanitizeHtmlResult with sanitized content and details
     */
    SanitizeHtmlResult sanitizeHtml(String htmlContent);
    
    /**
     * Check if template name is unique for the client
     * @param templateName Name to check
     * @return true if unique
     */
    boolean isTemplateNameUnique(String templateName);
}
