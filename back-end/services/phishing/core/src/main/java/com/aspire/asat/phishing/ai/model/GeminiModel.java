package com.aspire.asat.phishing.ai.model;

import java.util.Arrays;

/**
 * Allowed Gemini models with a single default fallback.
 */
public enum GeminiModel {
    GEMINI_3_FLASH("gemini-3-flash", false),
    GEMINI_3_1_PRO("gemini-3.1-pro", false),
    GEMINI_3_1_FLASH_LITE_PREVIEW("gemini-3.1-flash-lite-preview", false),
    GEMINI_3_1_PRO_PREVIEW("gemini-3.1-pro-preview", false),
    GEMINI_2_5_PRO("gemini-2.5-pro", false),
    GEMINI_2_5_FLASH("gemini-2.5-flash", false),
    GEMINI_3_FLASH_PREVIEW("gemini-3-flash-preview", true);

    private final String value;
    private final boolean defaultModel;

    GeminiModel(String value, boolean defaultModel) {
        this.value = value;
        this.defaultModel = defaultModel;
    }

    public String getValue() {
        return value;
    }

    public static boolean isAllowed(String model) {
        if (model == null || model.isBlank()) {
            return false;
        }
        String normalized = model.trim();
        return Arrays.stream(values()).anyMatch(m -> m.value.equals(normalized));
    }

    public static String defaultModel() {
        return Arrays.stream(values())
                .filter(GeminiModel::isDefaultModel)
                .findFirst()
                .orElse(GEMINI_3_FLASH_PREVIEW)
                .getValue();
    }

    private boolean isDefaultModel() {
        return defaultModel;
    }
}
