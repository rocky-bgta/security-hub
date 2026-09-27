package com.aspire.asat.breachdetection.dto.enums;

public enum InsecureWebBreachStatus {
    OPEN,
    IN_MITIGATION,
    COMPLETED,
    UNKNOWN;

    public static InsecureWebBreachStatus fromExternal(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        try {
            return InsecureWebBreachStatus.valueOf(value.trim().toUpperCase().replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}
