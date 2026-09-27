package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.LandingPageDto;

import java.util.List;

/**
 * Manages many-to-many bindings between email/SMS templates and landing pages.
 * Bindings are stored on {@link com.aspire.asat.phishing.model.EmailTemplate#landingPageIds}.
 */
public interface EmailTemplateLandingPageBindingService {

    // Sets the landing pages for a given email template, replacing any existing bindings.
    void setLandingPagesForTemplate(String templateId, List<String> landingPageIds, String clientId);

    // when landing page created, add it to all templates of the client
    void addLandingPageToTemplates(String landingPageId, List<String> emailTemplateIds, String clientId);

    // when landing page updated, sync it to all templates of the client
    void syncTemplatesForLandingPage(String landingPageId, List<String> emailTemplateIds, String clientId);

    void removeLandingPageFromAllTemplates(String landingPageId);

    List<LandingPageDto> getBoundLandingPages(String templateId, String clientId);

    List<EmailTemplateDto> getBoundEmailTemplates(String landingPageId, String clientId);

    List<String> getBoundEmailTemplateIds(String landingPageId, String clientId);
}
