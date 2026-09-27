package com.aspire.asat.phishing.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Risk trend status and direction token (stored in DB as plain strings, not symbols).
 */
@Getter
@RequiredArgsConstructor
public enum RiskTrend {
    IMPROVING("Improving", "UP"),
    WORSENING("Worsening", "DOWN"),
    STABLE("Stable", "FLAT");

    private final String status;
    /** Direction token: UP, DOWN, or FLAT */
    private final String direction;

    public static RiskTrend fromDelta(double delta) {
        if (delta < 0) {
            return IMPROVING;
        }
        if (delta > 0) {
            return WORSENING;
        }
        return STABLE;
    }
}
