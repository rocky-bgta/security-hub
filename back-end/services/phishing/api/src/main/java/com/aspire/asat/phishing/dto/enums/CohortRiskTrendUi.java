package com.aspire.asat.phishing.dto.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Cohort risk trend label returned to the admin UI ("improving", "worsening", "stable").
 */
@Getter
@RequiredArgsConstructor
public enum CohortRiskTrendUi {
    IMPROVING("improving"),
    WORSENING("worsening"),
    STABLE("stable");

    private final String apiValue;

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }

    public static CohortRiskTrendUi fromAggregateStatus(String status) {
        if (status == null || status.isBlank()) {
            return STABLE;
        }
        return switch (status.trim()) {
            case "Improving" -> IMPROVING;
            case "Worsening" -> WORSENING;
            case "Stable" -> STABLE;
            default -> STABLE;
        };
    }
}
