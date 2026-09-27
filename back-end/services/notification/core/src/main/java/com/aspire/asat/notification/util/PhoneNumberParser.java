package com.aspire.asat.notification.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for parsing phone numbers and extracting country codes
 * Supports country code detection for provider selection
 */
@Slf4j
@Component
public class PhoneNumberParser {

    // Common country code mappings (can be extended)
    private static final Map<String, String> COUNTRY_CODE_MAP = new HashMap<>();
    
    static {
        // Bangladesh
        COUNTRY_CODE_MAP.put("+880", "BD");
        // North America (US, Canada)
        COUNTRY_CODE_MAP.put("+1", "NA");
        // UK
        COUNTRY_CODE_MAP.put("+44", "GB");
        // India
        COUNTRY_CODE_MAP.put("+91", "IN");
        // Add more as needed
    }

    // Pattern to match international phone numbers (E.164 format)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+\\d{1,4}\\d{7,15}$");

    /**
     * Extract country code from phone number
     * @param phoneNumber Phone number in E.164 format (e.g., +8801712345678, +12125551234)
     * @return Country code (e.g., "+880", "+1") or null if not found
     */
    public String extractCountryCode(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        String normalized = phoneNumber.trim();
        
        // Must start with +
        if (!normalized.startsWith("+")) {
            log.warn("Phone number does not start with +: {}", phoneNumber);
            return null;
        }

        // Extract country code (typically 1-4 digits after +)
        // Try to match known country codes first
        for (String countryCode : COUNTRY_CODE_MAP.keySet()) {
            if (normalized.startsWith(countryCode)) {
                return countryCode;
            }
        }

        // Fallback: extract first 1-4 digits after +
        // This is a simple heuristic and may not work for all cases
        int maxDigits = Math.min(4, normalized.length() - 1);
        for (int i = 1; i <= maxDigits; i++) {
            String potentialCode = "+" + normalized.substring(1, 1 + i);
            if (COUNTRY_CODE_MAP.containsKey(potentialCode)) {
                return potentialCode;
            }
        }

        // If no match found, try to extract first 1-4 digits as country code
        if (normalized.length() > 1) {
            int digitsToExtract = Math.min(4, normalized.length() - 1);
            return "+" + normalized.substring(1, 1 + digitsToExtract);
        }

        return null;
    }

    /**
     * Get country identifier from country code
     * @param countryCode Country code (e.g., "+880", "+1")
     * @return Country identifier (e.g., "BD", "NA") or null if not found
     */
    public String getCountryIdentifier(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        return COUNTRY_CODE_MAP.get(countryCode);
    }

    /**
     * Validate phone number format (E.164 format)
     * @param phoneNumber Phone number to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    /**
     * Normalize phone number (remove spaces, dashes, etc.)
     * @param phoneNumber Phone number to normalize
     * @return Normalized phone number
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        // Remove spaces, dashes, parentheses
        return phoneNumber.replaceAll("[\\s\\-\\(\\)]", "").trim();
    }

    /**
     * Detect country from phone number and return country identifier
     * @param phoneNumber Phone number in E.164 format
     * @return Country identifier (e.g., "BD", "NA") or null if not found
     */
    public String detectCountry(String phoneNumber) {
        String normalized = normalizePhoneNumber(phoneNumber);
        String countryCode = extractCountryCode(normalized);
        return getCountryIdentifier(countryCode);
    }
}
