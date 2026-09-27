package com.aspire.asat.registration.utils;

import lombok.experimental.UtilityClass;

/**
 * Utility class containing default MSP data constants and helper methods.
 * Used for buy now onboarding flows when MSP information is not provided.
 */
@UtilityClass
public class DefaultMspData {

    /**
     * Default MSP ID for Aspire
     * Fixed ID to ensure consistency across application restarts
     */
    public static final String DEFAULT_MSP_ID = "ASPIRE-DEFAULT";

    /**
     * Default MSP organization name
     */
    public static final String DEFAULT_MSP_NAME = "Aspire";

    /**
     * Default MSP admin email
     */
    public static final String DEFAULT_MSP_EMAIL = "info@aspiretss.com";

    /**
     * Default MSP status
     */
    public static final String DEFAULT_MSP_STATUS = "ACTIVE";

    /**
     * Default system user identifier for created by field
     */
    public static final String DEFAULT_CREATED_BY = "SYSTEM";

    /**
     * Default notes for system-created MSP
     */
    public static final String DEFAULT_MSP_NOTES = "Default MSP initialized by system for buy now onboarding";

    /**
     * Get the default MSP ID
     * @return the default MSP ID
     */
    public static String getDefaultMspId() {
        return DEFAULT_MSP_ID;
    }

    /**
     * Get the default MSP name
     * @return the default MSP name
     */
    public static String getDefaultMspName() {
        return DEFAULT_MSP_NAME;
    }

    /**
     * Get the default MSP email
     * @return the default MSP email
     */
    public static String getDefaultMspEmail() {
        return DEFAULT_MSP_EMAIL;
    }

    /**
     * Get the default MSP status
     * @return the default MSP status
     */
    public static String getDefaultMspStatus() {
        return DEFAULT_MSP_STATUS;
    }
}

