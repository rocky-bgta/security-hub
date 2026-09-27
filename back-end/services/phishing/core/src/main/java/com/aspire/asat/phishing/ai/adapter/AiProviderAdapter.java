package com.aspire.asat.phishing.ai.adapter;

import com.aspire.asat.phishing.ai.model.AiEmailRequest;
import com.aspire.asat.phishing.ai.model.AiFixHtmlPageRequest;
import com.aspire.asat.phishing.ai.model.AiGeneratedEmailContent;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.model.AiGeneratedLandingContent;
import com.aspire.asat.phishing.ai.model.AiLandingPageRequest;
import com.aspire.asat.phishing.dto.enums.AiProviderType;

/**
 * Provider-agnostic adapter contract for AI content generation.
 * Concrete implementations encapsulate provider-specific SDK/API logic.
 */
public interface AiProviderAdapter {

    /**
     * @return provider type handled by this adapter implementation
     */
    AiProviderType getProviderType();

    /**
     * Generates phishing email template content using provider-specific AI models.
     *
     * @param request normalized email generation request
     * @return generated email content payload
     */
    AiGeneratedEmailContent generateEmailTemplate(AiEmailRequest request);

    /**
     * Generates phishing landing page content using provider-specific AI models.
     *
     * @param request normalized landing page generation request
     * @return generated landing page content payload
     */
    AiGeneratedLandingContent generateLandingPage(AiLandingPageRequest request);

    /**
     * Fixes a specific HTML section and returns an updated full HTML page.
     *
     * @param request normalized html-fix request
     * @param credentials resolved API key/secret (never from the public HTTP body)
     * @return updated HTML page markup
     */
    String fixHtmlPage(AiFixHtmlPageRequest request, AiResolvedCredentials credentials);

    /**
     * Fixes a specific HTML section and returns an updated full HTML email template.
     *
     * @param request normalized html-fix request
     * @param credentials resolved API key/secret (never from the public HTTP body)
     * @return updated HTML email template markup
     */
    String fixEmailTemplate(AiFixHtmlPageRequest request, AiResolvedCredentials credentials);
}
