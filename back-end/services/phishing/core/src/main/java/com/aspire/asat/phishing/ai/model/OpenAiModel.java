package com.aspire.asat.phishing.ai.model;

import java.util.Arrays;

/**
 * Allowed OpenAI models with a single default fallback.
 */
public enum OpenAiModel {
    GPT_4O_MINI("gpt-4o-mini", true),
    GPT_4_1_MINI("gpt-4.1-mini", false),
    GPT_4O("gpt-4o", false),
    GPT_4_1("gpt-4.1", false),
    GPT_5_4("gpt-5.4", false),
    GPT_5_4_MINI("gpt-5.4-mini", false),
    GPT_5_3("gpt-5.3", false);

    private final String value;
    private final boolean defaultModel;

    OpenAiModel(String value, boolean defaultModel) {
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
                .filter(OpenAiModel::isDefaultModel)
                .findFirst()
                .orElse(GPT_4O_MINI)
                .getValue();
    }

    private boolean isDefaultModel() {
        return defaultModel;
    }
}
