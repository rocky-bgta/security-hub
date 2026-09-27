package com.aspire.asat.phishing.ai.model;

import java.util.Arrays;

/**
 * Allowed Z AI models with a single default fallback.
 */
public enum ZAiModel {
    GLM_5_1("glm-5.1", true),
    GLM_5("glm-5", false),
    GLM_5_TURBO("glm-5-turbo", false),
    GLM_4_7("glm-4.7", false),
    GLM_4_6("glm-4.6", false),
    GLM_4_5("glm-4.5", false);

    private final String value;
    private final boolean defaultModel;

    ZAiModel(String value, boolean defaultModel) {
        this.value = value;
        this.defaultModel = defaultModel;
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
                .filter(ZAiModel::isDefaultModel)
                .findFirst()
                .orElse(GLM_5_1)
                .value;
    }

    private boolean isDefaultModel() {
        return defaultModel;
    }
}
