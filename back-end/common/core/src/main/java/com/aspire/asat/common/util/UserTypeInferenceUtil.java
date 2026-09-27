package com.aspire.asat.common.util;

import com.aspire.asat.common.enums.UserType;
import org.springframework.util.StringUtils;

/**
 * Shared utility for inferring and normalizing UserType when saving activity logs.
 * Used by activity log aspects, auth service (login/logout), and any API that creates activity logs
 * so ASPIRE_ADMIN and MSP are never wrongly saved as SYSTEM_USER (e.g. legacy data or missing header).
 */
public final class UserTypeInferenceUtil {

    private UserTypeInferenceUtil() {
    }

    /**
     * Infer UserType from string (handles "ASPIRE_ADMIN", "Aspire Admin", "aspire_admin", etc.).
     * Use when userType is provided as a string (e.g. from header or context).
     */
    public static UserType inferUserTypeFromString(String userTypeStr) {
        if (userTypeStr == null || userTypeStr.isBlank()) {
            return null;
        }
        String s = userTypeStr.trim();
        if (UserType.SUPER_ADMIN.getValue().equalsIgnoreCase(s)) {
            return UserType.SUPER_ADMIN;
        }
        if (UserType.ASPIRE_ADMIN.getValue().equalsIgnoreCase(s)) {
            return UserType.ASPIRE_ADMIN;
        }
        if (UserType.SYSTEM_USER.getValue().equalsIgnoreCase(s)) {
            return UserType.SYSTEM_USER;
        }
        if (UserType.MSP.getValue().equalsIgnoreCase(s)) {
            return UserType.MSP;
        }
        if (UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(s)) {
            return UserType.CLIENT_ADMIN;
        }
        if (UserType.CLIENT.getValue().equalsIgnoreCase(s)) {
            return UserType.CLIENT;
        }
        if (UserType.USER.getValue().equalsIgnoreCase(s)) {
            return UserType.USER;
        }
        // Try normalized form (e.g. "Aspire Admin" -> "ASPIRE_ADMIN")
        try {
            String normalized = s.replace(' ', '_').toUpperCase();
            return UserType.fromString(normalized);
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    /**
     * Infer UserType from context when userType is null (e.g. legacy data or missing header).
     * Uses userId vs mspId/clientAdminId so ASPIRE_ADMIN and MSP get correct activity log.
     * Use with CurrentUserContext, AspireUser, or CreateActivityLogDto (userId + mspId + clientAdminId).
     */
    public static UserType inferUserTypeFromContext(String userId, String mspId, String clientAdminId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        if (StringUtils.hasText(mspId) && mspId.equals(userId)) {
            return UserType.MSP;
        }
        if (StringUtils.hasText(clientAdminId) && clientAdminId.equals(userId)) {
            return UserType.CLIENT_ADMIN;
        }
        // userId present but no mspId/clientAdminId match -> likely Aspire Admin or Super Admin
        if (!StringUtils.hasText(mspId) && !StringUtils.hasText(clientAdminId)) {
            return UserType.ASPIRE_ADMIN;
        }
        return null;
    }

    /**
     * Normalize userType string for consistent comparison (handles "Aspire_Admin", "aspire_admin", etc.).
     */
    public static String normalizeUserTypeString(String userTypeStr) {
        if (userTypeStr == null || userTypeStr.isBlank()) {
            return "";
        }
        return userTypeStr.trim().replace(' ', '_').toUpperCase();
    }
}
