package com.aspire.asat.common.enums;

import lombok.Getter;

/**
 * Enum representing different types of users in the centralized system
 */
@Getter
public enum UserType {
    ASPIRE_ADMIN("ASPIRE_ADMIN"),
    CLIENT("CLIENT"),
    CLIENT_ADMIN("CLIENT_ADMIN"),
    MSP("MSP"),
    USER("USER"), // END User
    SUPER_ADMIN("SUPER_ADMIN"),
    SYSTEM_USER("SYSTEM_USER") // Internal system user for automated processes
    ;

    private final String value;

    UserType(String value) {
        this.value = value;
    }

    public static UserType fromString(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("UserType text cannot be null or blank");
        }
        String trimmed = text.trim();
        // Match by value (e.g. "ASPIRE_ADMIN", "MSP")
        for (UserType type : UserType.values()) {
            if (type.value.equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        // Match by enum name (e.g. same as value for most)
        for (UserType type : UserType.values()) {
            if (type.name().equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        // Normalize: "Aspire Admin" -> "ASPIRE_ADMIN" so we recognize display-style values
        String normalized = trimmed.replace(' ', '_').toUpperCase();
        for (UserType type : UserType.values()) {
            if (type.value.equalsIgnoreCase(normalized) || type.name().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}

