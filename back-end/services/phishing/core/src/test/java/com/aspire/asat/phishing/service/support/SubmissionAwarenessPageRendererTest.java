package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.config.SubmissionAwarenessProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmissionAwarenessPageRendererTest {

    private SubmissionAwarenessPageRenderer renderer;

    @BeforeEach
    void setUp() {
        SubmissionAwarenessProperties properties = new SubmissionAwarenessProperties();
        properties.setGuideUrl("https://guide.example.com/awareness");
        renderer = new SubmissionAwarenessPageRenderer(
                properties,
                new RedirectUrlSanitizer()
        );
    }

    @Test
    void render_containsAwarenessMessage() {
        String html = renderer.render("https://redirect.example.com", 3);

        assertTrue(html.contains("You just entered sensitive information into a simulated phishing page."));
        assertTrue(html.contains("In a real attack, this could have allowed criminals to access your accounts."));
        assertTrue(html.contains("Never share OTPs or account details on suspicious websites."));
    }

    @Test
    void render_containsCountdownAndDelay() {
        String html = renderer.render("https://redirect.example.com", 3);

        assertTrue(html.contains("id=\"countdown\""));
        assertTrue(html.contains("Redirecting in"));
        assertTrue(html.contains("setTimeout"));
        assertTrue(html.contains("3000"));
        assertTrue(html.contains("http-equiv=\"refresh\""));
        assertTrue(html.contains("content=\"3;url=https://redirect.example.com\""));
    }

    @Test
    void render_encodesRedirectUrlSafelyInScript() {
        String malicious = "https://example.com/\" onmouseover=\"alert(1)";
        String html = renderer.render(malicious, 3);

        assertTrue(html.contains("\"https://example.com/\\\" onmouseover=\\\"alert(1)\""));
        assertFalse(html.contains("onmouseover=\"alert(1)\">"));
    }

    @Test
    void render_includesGuideLinkWhenConfigured() {
        String html = renderer.render("https://redirect.example.com", 3);

        assertTrue(html.contains("href=\"https://guide.example.com/awareness\""));
        assertTrue(html.contains("Review our phishing awareness guide</a>"));
    }

    @Test
    void render_omitsGuideLinkWhenNotConfigured() {
        SubmissionAwarenessProperties properties = new SubmissionAwarenessProperties();
        properties.setGuideUrl("");
        SubmissionAwarenessPageRenderer noGuideRenderer = new SubmissionAwarenessPageRenderer(
                properties,
                new RedirectUrlSanitizer()
        );

        String html = noGuideRenderer.render("https://redirect.example.com", 3);

        assertTrue(html.contains("Review our phishing awareness guide to strengthen your defenses."));
        assertFalse(html.contains("<a href="));
    }
}
