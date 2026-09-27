package com.aspire.asat.phishing.service.impl.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImportBlockDetectorTest {

    private final ImportBlockDetector detector = new ImportBlockDetector();

    @Test
    void shouldDetectCloudflareSignals() {
        String html = "<html><title>Attention Required</title><body>cf-chl-widget and cf-ray token</body></html>";
        ImportBlockDetector.DetectionResult result = detector.detect(html, "Attention Required", 403);

        Assertions.assertTrue(result.isBlocked());
        Assertions.assertEquals("waf_challenge", result.getReason());
        Assertions.assertTrue(result.getSignals().contains("cloudflare"));
    }

    @Test
    void shouldNotMarkNormalHtmlAsBlocked() {
        String html = "<html><title>Home</title><body><h1>Welcome</h1></body></html>";
        ImportBlockDetector.DetectionResult result = detector.detect(html, "Home", 200);

        Assertions.assertFalse(result.isBlocked());
        Assertions.assertNull(result.getReason());
        Assertions.assertTrue(result.getSignals().isEmpty());
    }

    @Test
    void shouldClassifyCaptchaAsBotChallenge() {
        String html = "<html><title>Verify you are human</title><body>Please solve captcha</body></html>";
        ImportBlockDetector.DetectionResult result = detector.detect(html, "Verify you are human", 200);

        Assertions.assertTrue(result.isBlocked());
        Assertions.assertEquals("bot_or_human_challenge", result.getReason());
        Assertions.assertTrue(result.getSignals().contains("captcha_or_human_check"));
    }
}

