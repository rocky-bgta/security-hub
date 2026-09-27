package com.aspire.asat.notification.enums;

import lombok.Getter;

/**
 * Overall delivery status of a notification across all channels
 */
@Getter
public enum DeliveryStatus {
    SUCCESS("success", "All channels succeeded"),
    PARTIAL("partial", "Some channels succeeded, some failed"),
    FAILED("failed", "All channels failed"),
    PENDING("pending", "Still processing");
    
    private final String code;
    private final String description;
    
    DeliveryStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get delivery status by code
     */
    public static DeliveryStatus fromCode(String code) {
        for (DeliveryStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown delivery status code: " + code);
    }
}

