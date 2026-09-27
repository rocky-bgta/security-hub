package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.AITemplateGenerateRequest;
import com.aspire.asat.phishing.dto.request.FixEmailTemplateRequest;

import java.util.List;

/**
 * Service interface for AI-powered email template content generation.
 * Based on BRD Use Case 2.1.3.2: AI Template Generation
 */
public interface AIContentGeneratorService {
    
    /**
     * Generate AI content for an email template using the current HTTP request user context
     * to resolve the tenant key for credential lookup.
     *
     * @param request AI generation request with context parameters
     * @return AIGeneratedContent with HTML body, text body, and suggested difficulty
     */
    AIGeneratedContent generateTemplateContent(AITemplateGenerateRequest request);

    /**
     * Same as {@link #generateTemplateContent(AITemplateGenerateRequest)} but accepts the
     * tenant key directly. Required for the asynchronous SQS worker because there is no HTTP
     * request scope from which to read the user-context header.
     *
     * @param request AI generation request stored on the job document
     * @param clientAdminId tenant key for credential lookup
     * @return AIGeneratedContent with HTML body, text body, and suggested difficulty
     */
    AIGeneratedContent generateTemplateContent(AITemplateGenerateRequest request, String clientAdminId);

    /**
     * Fixes a selected element in an email template and returns updated full HTML template.
     *
     * @param request provider/model + prompt + element/template html input
     * @return updated full html email template content
     */
    String fixEmailTemplate(FixEmailTemplateRequest request);
    
    /**
     * Get available target industries
     * @return List of supported industries
     */
    List<String> getTargetIndustries();
    
    /**
     * Get available attacker personas
     * @return List of attacker personas (15+)
     */
    List<String> getAttackerPersonas();
    
    /**
     * Get available social engineering strategies
     * @return List of social engineering strategies (15+)
     */
    List<String> getSocialEngineeringStrategies();
    
    /**
     * Get available campaign objectives
     * @return List of campaign objectives
     */
    List<String> getCampaignObjectives();
    
    /**
     * DTO for AI-generated content result
     */
    record AIGeneratedContent(
            String htmlBody,
            String textBody,
            String suggestedDifficulty,
            boolean success,
            String errorMessage
    ) {}
}
