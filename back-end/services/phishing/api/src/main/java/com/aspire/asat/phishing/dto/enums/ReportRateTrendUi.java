package com.aspire.asat.phishing.dto.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * Report-rate micro-trend values returned to the admin UI ("up", "down", "stable").
 */
@Getter
@RequiredArgsConstructor
public enum ReportRateTrendUi {
    UP("up"),
    DOWN("down"),
    STABLE("stable");

    private final String apiValue;

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }

    public static ReportRateTrendUi fromStoredDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return STABLE;
        }
        return switch (direction.trim().toUpperCase(Locale.ROOT)) {
            case "UP" -> UP;
            case "DOWN" -> DOWN;
            default -> STABLE;
        };
    }
}
