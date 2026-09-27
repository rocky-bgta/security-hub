package com.aspire.asat.registration.enums;

import lombok.Getter;

/**
 * Enum for Login History filter types
 */
@Getter
public enum LoginHistoryFilterType {
    /**
     * Last 7 days - returns daily data
     */
    SEVEN_DAYS("7Day"),
    
    /**
     * Last 1 month - returned weekly data (5 weeks)
     */
    ONE_MONTH("1Month"),
    
    /**
     * Last 12 months - returns monthly data (12 months)
     */
    TWELVE_MONTHS("12Month");
    
    private final String value;
    
    LoginHistoryFilterType(String value) {
        this.value = value;
    }

    /**
     * Get enum from string value
     */
    public static LoginHistoryFilterType fromValue(String value) {
        for (LoginHistoryFilterType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid filter type: " + value);
    }
}
