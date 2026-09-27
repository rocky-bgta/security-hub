package com.aspire.asat.phishing.service.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RedirectUrlSanitizerTest {

    private final RedirectUrlSanitizer sanitizer = new RedirectUrlSanitizer();

    @Test
    void sanitize_allowsHttpsUrl() {
        assertEquals("https://example.com/path", sanitizer.sanitize("https://example.com/path"));
    }

    @Test
    void sanitize_allowsHttpUrl() {
        assertEquals("http://example.com", sanitizer.sanitize("http://example.com"));
    }

    @Test
    void sanitize_rejectsJavascriptUrl() {
        assertEquals("/", sanitizer.sanitize("javascript:alert(1)"));
    }

    @Test
    void sanitize_rejectsDataUrl() {
        assertEquals("/", sanitizer.sanitize("data:text/html,<script>alert(1)</script>"));
    }

    @Test
    void sanitize_rejectsProtocolRelativeUrl() {
        assertEquals("/", sanitizer.sanitize("//evil.com"));
    }

    @Test
    void sanitize_rejectsBlankAndNull() {
        assertEquals("/", sanitizer.sanitize(null));
        assertEquals("/", sanitizer.sanitize(""));
        assertEquals("/", sanitizer.sanitize("   "));
    }

    @Test
    void sanitizeOptional_returnsNullForInvalid() {
        assertNull(sanitizer.sanitizeOptional("javascript:alert(1)"));
        assertNull(sanitizer.sanitizeOptional(""));
    }

    @Test
    void sanitizeOptional_returnsValidUrl() {
        assertEquals("https://guide.example.com", sanitizer.sanitizeOptional("https://guide.example.com"));
    }
}
