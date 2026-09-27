package com.aspire.asat.common.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * E.164 phone number normalization and validation utilities.
 */
public final class PhoneNumberUtils {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+\\d{7,15}$");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    /** Maps E.164 dialing prefix to ISO 3166-1 alpha-2 country code. */
    private static final Map<String, String> COUNTRY_CODE_MAP = new HashMap<>();

    /**
     * Prefixes sorted longest-first so +880 matches before +88, +971 before +97, etc.
     */
    private static final String[] DIALING_PREFIXES;

    static {
        // North America (NANP — US, Canada, and Caribbean territories share +1)
        COUNTRY_CODE_MAP.put("+1", "US");

        // Latin America & Caribbean
        COUNTRY_CODE_MAP.put("+52", "MX");   // Mexico
        COUNTRY_CODE_MAP.put("+55", "BR");   // Brazil
        COUNTRY_CODE_MAP.put("+54", "AR");   // Argentina
        COUNTRY_CODE_MAP.put("+56", "CL");   // Chile
        COUNTRY_CODE_MAP.put("+57", "CO");   // Colombia
        COUNTRY_CODE_MAP.put("+51", "PE");   // Peru
        COUNTRY_CODE_MAP.put("+58", "VE");   // Venezuela

        // Western Europe
        COUNTRY_CODE_MAP.put("+44", "GB");   // United Kingdom
        COUNTRY_CODE_MAP.put("+49", "DE");   // Germany
        COUNTRY_CODE_MAP.put("+33", "FR");   // France
        COUNTRY_CODE_MAP.put("+39", "IT");   // Italy
        COUNTRY_CODE_MAP.put("+34", "ES");   // Spain
        COUNTRY_CODE_MAP.put("+31", "NL");   // Netherlands
        COUNTRY_CODE_MAP.put("+41", "CH");   // Switzerland
        COUNTRY_CODE_MAP.put("+43", "AT");   // Austria
        COUNTRY_CODE_MAP.put("+32", "BE");   // Belgium
        COUNTRY_CODE_MAP.put("+351", "PT");  // Portugal
        COUNTRY_CODE_MAP.put("+353", "IE");  // Ireland
        COUNTRY_CODE_MAP.put("+46", "SE");   // Sweden
        COUNTRY_CODE_MAP.put("+47", "NO");   // Norway
        COUNTRY_CODE_MAP.put("+45", "DK");   // Denmark
        COUNTRY_CODE_MAP.put("+358", "FI");  // Finland
        COUNTRY_CODE_MAP.put("+48", "PL");   // Poland
        COUNTRY_CODE_MAP.put("+30", "GR");   // Greece
        COUNTRY_CODE_MAP.put("+420", "CZ");  // Czech Republic
        COUNTRY_CODE_MAP.put("+36", "HU");   // Hungary
        COUNTRY_CODE_MAP.put("+40", "RO");   // Romania

        // Eastern Europe & Central Asia
        COUNTRY_CODE_MAP.put("+380", "UA");  // Ukraine
        COUNTRY_CODE_MAP.put("+7", "RU");    // Russia / Kazakhstan

        // Middle East
        COUNTRY_CODE_MAP.put("+971", "AE");  // United Arab Emirates
        COUNTRY_CODE_MAP.put("+966", "SA");  // Saudi Arabia
        COUNTRY_CODE_MAP.put("+972", "IL");  // Israel
        COUNTRY_CODE_MAP.put("+90", "TR");   // Turkey
        COUNTRY_CODE_MAP.put("+20", "EG");   // Egypt

        // South & Southeast Asia
        COUNTRY_CODE_MAP.put("+880", "BD");  // Bangladesh
        COUNTRY_CODE_MAP.put("+91", "IN");   // India
        COUNTRY_CODE_MAP.put("+92", "PK");   // Pakistan
        COUNTRY_CODE_MAP.put("+86", "CN");   // China
        COUNTRY_CODE_MAP.put("+81", "JP");   // Japan
        COUNTRY_CODE_MAP.put("+82", "KR");   // South Korea
        COUNTRY_CODE_MAP.put("+65", "SG");  // Singapore
        COUNTRY_CODE_MAP.put("+60", "MY");   // Malaysia
        COUNTRY_CODE_MAP.put("+62", "ID");   // Indonesia
        COUNTRY_CODE_MAP.put("+63", "PH");   // Philippines
        COUNTRY_CODE_MAP.put("+66", "TH");   // Thailand
        COUNTRY_CODE_MAP.put("+84", "VN");   // Vietnam

        // East Asia
        COUNTRY_CODE_MAP.put("+852", "HK"); // Hong Kong
        COUNTRY_CODE_MAP.put("+886", "TW");  // Taiwan

        // Oceania
        COUNTRY_CODE_MAP.put("+61", "AU");   // Australia
        COUNTRY_CODE_MAP.put("+64", "NZ");   // New Zealand

        // Africa
        COUNTRY_CODE_MAP.put("+27", "ZA");   // South Africa
        COUNTRY_CODE_MAP.put("+234", "NG");  // Nigeria
        COUNTRY_CODE_MAP.put("+254", "KE");  // Kenya

        DIALING_PREFIXES = COUNTRY_CODE_MAP.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toArray(String[]::new);
    }

    private PhoneNumberUtils() {
    }

    public static boolean isValidE164(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        String normalized = normalize(phoneNumber);
        return normalized != null && E164_PATTERN.matcher(normalized).matches();
    }

    public static String normalize(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String trimmed = phoneNumber.trim().replaceAll("[\\s()-]", "");
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!trimmed.startsWith("+")) {
            if (trimmed.startsWith("00")) {
                trimmed = "+" + trimmed.substring(2);
            } else if (trimmed.startsWith("0") && trimmed.length() > 10) {
                trimmed = "+880" + trimmed.substring(1);
            } else if (trimmed.matches("\\d{10,15}")) {
                trimmed = "+" + trimmed;
            } else {
                return null;
            }
        }
        // Bangladesh local numbers are stored with a trunk 0 after +880 (e.g. +8800177...).
        if (trimmed.startsWith("+8800") && trimmed.length() > 8) {
            trimmed = "+880" + trimmed.substring(5);
        }
        return trimmed;
    }

    /**
     * Returns the matched E.164 dialing prefix (e.g. {@code +880}, {@code +1}).
     */
    public static String extractCountryCode(String phoneNumber) {
        String normalized = normalize(phoneNumber);
        if (normalized == null) {
            return null;
        }
        for (String prefix : DIALING_PREFIXES) {
            if (normalized.startsWith(prefix)) {
                return prefix;
            }
        }
        if (normalized.length() >= 3) {
            return normalized.substring(0, Math.min(4, normalized.length()));
        }
        return null;
    }

    /**
     * Returns ISO 3166-1 alpha-2 country code for the phone number, or {@code null} if unknown.
     * Note: {@code +1} maps to {@code US}; Canada also uses {@code +1} (NANP) and cannot be
     * distinguished by dialing prefix alone.
     */
    public static String getCountryIso(String phoneNumber) {
        String prefix = extractCountryCode(phoneNumber);
        return prefix == null ? null : COUNTRY_CODE_MAP.get(prefix);
    }

    public static boolean containsHtml(String text) {
        return text != null && HTML_TAG_PATTERN.matcher(text).find();
    }
}
