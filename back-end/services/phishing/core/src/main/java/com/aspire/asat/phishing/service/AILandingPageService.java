package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.AILandingPageRequest;
import com.aspire.asat.phishing.dto.request.FixHtmlPageRequest;
import com.aspire.asat.phishing.dto.response.LandingPageDto;

/**
 * Service interface for AI-powered landing page generation.
 * Based on Task-05 Landing Page Creation
 */
public interface AILandingPageService {
    
    /**
     * Generate a landing page using AI
     * @param request AI generation request with parameters
     * @return Generated LandingPageDto
     */
    LandingPageDto generateLandingPage(AILandingPageRequest request);
    
    /**
     * Regenerate landing page content (BR-04: unlimited regeneration)
     * @param request AI generation request with parameters
     * @return Regenerated LandingPageDto
     */
    LandingPageDto regenerateLandingPage(AILandingPageRequest request);
    
    /**
     * Generate HTML content based on generation mode
     * @param mode Generation mode (CLONE_STYLE, BRAND_BASED, FULLY_AI)
     * @param params Additional parameters
     * @return Generated HTML content
     */
    String generateHtmlContent(String mode, java.util.Map<String, String> params);

    /**
     * Resolve credentials and call the configured AI provider to render landing page HTML
     * for the supplied request, then return sanitized HTML ready to persist.
     *
     * <p>Intended for the asynchronous SQS worker; the synchronous controller path
     * uses {@link #generateLandingPage(AILandingPageRequest)} which only enqueues the job.
     *
     * @param request the original AI generation request stored on the job document
     * @param clientAdminId tenant key for credential lookup; the worker reads this from the job doc
     *                      because it has no HTTP request context to derive it from headers
     * @return sanitized HTML content
     */
    String generateLandingPageHtml(AILandingPageRequest request, String clientAdminId);

    /**
     * Fixes a section of an existing HTML page and returns updated HTML.
     *
     * @param request provider/model/credential + fix instructions
     * @return updated full HTML page content
     */
    String fixHtmlPage(FixHtmlPageRequest request);
}
