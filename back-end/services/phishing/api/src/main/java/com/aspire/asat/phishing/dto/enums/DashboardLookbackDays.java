package com.aspire.asat.phishing.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Allowed lookback windows for dashboard endpoints (KPI metrics, phish-prone data, etc.).
 */
@Getter
@RequiredArgsConstructor
public enum DashboardLookbackDays {

    DAYS_7(7),
    DAYS_14(14),
    DAYS_30(30),
    DAYS_90(90),
    DAYS_365(365);

    private final int days;

    public static DashboardLookbackDays resolve(int days) {
        for (DashboardLookbackDays value : values()) {
            if (value.days == days) {
                return value;
            }
        }
        String allowed = Arrays.stream(values())
                .map(v -> String.valueOf(v.days))
                .collect(Collectors.joining(", "));
        throw new IllegalArgumentException(
                "Invalid days value: " + days + ". Allowed values: " + allowed);
    }
}
