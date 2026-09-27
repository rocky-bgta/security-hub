package com.aspire.asat.common.enums.notification;

import lombok.Getter;

/**
 * Roles that can receive a notification and be individually gated in the
 * role-based notification settings matrix (Aspire Admin, MSP, Client Admin, User).
 */
@Getter
public enum NotificationRecipientRole {
    ASPIRE_ADMIN,
    MSP,
    CLIENT_ADMIN,
    USER;

    /**
     * Map a registration-side {@code AspireUser.userType} string onto a recipient role.
     * {@code SUPER_ADMIN} and {@code SYSTEM_USER} are treated as {@code ASPIRE_ADMIN};
     * {@code CLIENT} is treated as {@code CLIENT_ADMIN}.
     */
    public static NotificationRecipientRole fromUserType(String userType) {
        if (userType == null || userType.isBlank()) {
            return null;
        }
        String normalized = userType.trim().toUpperCase();
        return switch (normalized) {
            case "ASPIRE_ADMIN", "SUPER_ADMIN", "SYSTEM_USER" -> ASPIRE_ADMIN;
            case "MSP" -> MSP;
            case "CLIENT_ADMIN", "CLIENT" -> CLIENT_ADMIN;
            case "USER" -> USER;
            default -> null;
        };
    }
}
