package com.aspire.asat.common.enums.notification;

import lombok.Getter;

/**
 * Categories for organizing notification types in the ASAT platform
 */
@Getter
public enum NotificationCategory {
    USER_MANAGEMENT("User Management"),
    CONTENT_MANAGEMENT("Content Management"), 
    PACKAGE_MANAGEMENT("Package Management"),
    PAYMENT_MANAGEMENT("Payment Management"),
    POLICY_MANAGEMENT("Policy Management"),
    CERTIFICATE_MANAGEMENT("Certificate Management"),
    SYSTEM_OPERATIONAL("System/Operational"),
    GENERAL("General");
    
    private final String displayName;
    
    NotificationCategory(String displayName) {
        this.displayName = displayName;
    }

}
