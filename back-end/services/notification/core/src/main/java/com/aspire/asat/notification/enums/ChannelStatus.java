package com.aspire.asat.notification.enums;

import lombok.Getter;

/**
 * Status of notification delivery for a specific channel
 */
@Getter
public enum ChannelStatus {
    PENDING("pending", "Queued but not yet sent"),
    SUCCESS("success", "Successfully delivered"),
    FAILED("failed", "Delivery failed"),
    SKIPPED("skipped", "Skipped (e.g., channel disabled)");
    
    private final String code;
    private final String description;
    
    ChannelStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get channel status by code
     */
    public static ChannelStatus fromCode(String code) {
        for (ChannelStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown channel status code: " + code);
    }
}

