package com.aspire.asat.universal.utils;

/**
 * Utility class for checking user types.
 * Provides static methods to determine if a user is an end user, client admin, or MSP admin.
 */
public class UserTypeUtils {
    
    private UserTypeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if user is an end user/client user
     * 
     * @param userType The user type string to check
     * @return true if the user type is USER, END_USER, or CLIENT_USER (case-insensitive)
     */
    public static boolean isEndUser(String userType) {
        return "USER".equalsIgnoreCase(userType) || "END_USER".equalsIgnoreCase(userType) || "CLIENT_USER".equalsIgnoreCase(userType);
    }

    /**
     * Check if user is a client admin
     * 
     * @param userType The user type string to check
     * @return true if the user type is CLIENT_ADMIN or CLIENT (case-insensitive)
     */
    public static boolean isClientAdmin(String userType) {
        return "CLIENT_ADMIN".equalsIgnoreCase(userType) || "CLIENT".equalsIgnoreCase(userType);
    }

    /**
     * Check if user is an MSP admin
     * 
     * @param userType The user type string to check
     * @return true if the user type is MSP (case-insensitive)
     */
    public static boolean isMspAdmin(String userType) {
        return "MSP".equalsIgnoreCase(userType);
    }
}

