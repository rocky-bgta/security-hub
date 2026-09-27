package com.aspire.asat.phishing.constant;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Supported languages for voice input and template content.
 * Based on BRD Use Case 2.1.3.2: Multi-language voice input support (10 major languages)
 */
public final class SupportedLanguages {
    
    private SupportedLanguages() {
        // Prevent instantiation
    }
    
    /**
     * Map of language codes (ISO 639-1) to language names
     * 10 major languages from BRD
     */
    public static final Map<String, String> VOICE_INPUT_LANGUAGES;
    
    static {
        VOICE_INPUT_LANGUAGES = new LinkedHashMap<>();
        VOICE_INPUT_LANGUAGES.put("en", "English");
        VOICE_INPUT_LANGUAGES.put("zh", "Mandarin Chinese");
        VOICE_INPUT_LANGUAGES.put("hi", "Hindi");
        VOICE_INPUT_LANGUAGES.put("es", "Spanish");
        VOICE_INPUT_LANGUAGES.put("ar", "Arabic");
        VOICE_INPUT_LANGUAGES.put("fr", "French");
        VOICE_INPUT_LANGUAGES.put("bn", "Bengali");
        VOICE_INPUT_LANGUAGES.put("pt", "Portuguese");
        VOICE_INPUT_LANGUAGES.put("ru", "Russian");
        VOICE_INPUT_LANGUAGES.put("ur", "Urdu");
    }
    
    /**
     * Check if a language code is supported for voice input
     * @param languageCode ISO 639-1 language code
     * @return true if supported
     */
    public static boolean isSupported(String languageCode) {
        return VOICE_INPUT_LANGUAGES.containsKey(languageCode.toLowerCase());
    }
    
    /**
     * Get the language name for a given code
     * @param languageCode ISO 639-1 language code
     * @return Language name or null if not found
     */
    public static String getLanguageName(String languageCode) {
        return VOICE_INPUT_LANGUAGES.get(languageCode.toLowerCase());
    }
    
    /**
     * Get all supported language codes
     * @return Set of language codes
     */
    public static Set<String> getSupportedCodes() {
        return VOICE_INPUT_LANGUAGES.keySet();
    }
    
    /**
     * Minimum confidence score threshold for voice transcription
     * BR-08: Below this threshold, a warning should be shown
     */
    public static final double CONFIDENCE_THRESHOLD = 0.5;
    
    /**
     * Low confidence warning message
     */
    public static final String LOW_CONFIDENCE_WARNING = 
            "Low confidence transcription. Please try again or select language manually.";
}
