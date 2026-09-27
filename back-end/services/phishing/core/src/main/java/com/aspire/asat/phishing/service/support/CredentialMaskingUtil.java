package com.aspire.asat.phishing.service.support;

/**
 * Masks sensitive credential values for API responses and admin UI.
 */
public final class CredentialMaskingUtil {

    private CredentialMaskingUtil() {
    }

    public static String mask(String value) {
        if (value == null || value.isEmpty()) {
            return "****";
        }
        if (value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }
}
