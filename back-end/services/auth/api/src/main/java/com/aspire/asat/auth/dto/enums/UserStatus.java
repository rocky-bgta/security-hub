package com.aspire.asat.auth.dto.enums;

public enum UserStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    TEMPORARY_BLOCKED("TEMPORARY_BLOCKED");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public boolean isNotActive() {
        return this != ACTIVE;
    }

    public static UserStatus fromString(String text) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
