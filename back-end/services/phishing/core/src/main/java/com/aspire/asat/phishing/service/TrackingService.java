package com.aspire.asat.phishing.service;

import java.util.Map;

/**
 * Service for recording phishing tracking events triggered by
 * email opens, link clicks, landing page views, form submissions, and reports.
 */
public interface TrackingService {

    /**
     * Record an email-open event (triggered by invisible tracking pixel).
     */
    void recordOpen(String trackingId, String userAgent, String ipAddress);

    /**
     * Record a link-click event (triggered by rewritten URL redirect).
     */
    void recordClick(String trackingId, String userAgent, String ipAddress);

    /**
     * Record a click on the main phishing link, then return the
     * campaign's landing page HTML with form actions rewritten
     * to the submission tracking endpoint.
     *
     * @return instrumented landing page HTML, or {@code null} if not found
     */
    String serveLandingPage(String trackingId, String userAgent, String ipAddress);

    /**
     * Record a form submission on the landing page and capture the submitted data.
     *
     * @return the redirect URL configured on the landing page (may be {@code null})
     */
    String recordSubmission(String trackingId, Map<String, String> formData, String userAgent, String ipAddress);

    /**
     * Record that the recipient reported the email as phishing.
     */
    void recordReport(String trackingId, String userAgent, String ipAddress);

    /**
     * Record that the recipient reported the email as phishing, with optional
     * reporter metadata (e.g., Outlook add-in context).
     */
    void recordReport(String trackingId, String userAgent, String ipAddress, Map<String, Object> metadata);
}
