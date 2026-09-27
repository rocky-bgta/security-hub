package com.aspire.asat.cms.dto.enums;

/**
 * Enum for time frame options in sales progress analytics
 */
public enum TimeFrame {
    YEARLY,   // Shows year-wise data for previous 6 years (including current year)
    MONTHLY;  // Shows month-wise data for current year (Jan-Dec)

    /**
     * Convert string to TimeFrame enum (case-insensitive)
     * @param timeFrame string value
     * @return TimeFrame enum, defaults to MONTHLY
     */
    public static TimeFrame fromString(String timeFrame) {
        if (timeFrame == null || timeFrame.trim().isEmpty()) {
            return MONTHLY; // Default to MONTHLY
        }
        String normalized = timeFrame.trim().toUpperCase();
        try {
            return TimeFrame.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return MONTHLY; // Default to MONTHLY if invalid
        }
    }

    /**
     * Get string representation of the enum
     * @return string value (e.g., "Yearly", "Monthly")
     */
    public String getValue() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}

