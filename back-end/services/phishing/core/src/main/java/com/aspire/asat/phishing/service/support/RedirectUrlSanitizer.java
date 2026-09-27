package com.aspire.asat.phishing.service.support;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Validates redirect URLs for public tracking endpoints (http/https only).
 */
@Component
public class RedirectUrlSanitizer {

    private static final String FALLBACK_URL = "/";

    /**
     * @param url candidate redirect URL (may be null)
     * @return safe redirect target, or {@value #FALLBACK_URL} when invalid
     */
    public String sanitize(String url) {
        if (!StringUtils.hasText(url)) {
            return FALLBACK_URL;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return FALLBACK_URL;
    }

    /**
     * @return sanitized URL when valid http(s), otherwise {@code null} (for optional links)
     */
    public String sanitizeOptional(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return null;
    }
}
