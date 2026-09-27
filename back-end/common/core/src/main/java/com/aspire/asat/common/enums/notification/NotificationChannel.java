package com.aspire.asat.common.enums.notification;

import lombok.Getter;

/**
 * Available notification channels in the ASAT platform
 * Each notification can be delivered through multiple channels
 */
@Getter
public enum NotificationChannel {
    EMAIL("email", "Email", true),
    IN_APP("in_app", "In-App Notification", true),
    SMS("sms", "SMS", true), // SMS implementation
    PHONE_CALL("phone_call", "Phone Call", true), // Phone call implementation
    PUSH("push", "Push Notification", false); // Future implementation
    
    private final String code;
    private final String displayName;
    private final boolean currentlySupported;
    
    NotificationChannel(String code, String displayName, boolean currentlySupported) {
        this.code = code;
        this.displayName = displayName;
        this.currentlySupported = currentlySupported;
    }

    /**
     * Get notification channel by code
     */
    public static NotificationChannel fromCode(String code) {
        for (NotificationChannel channel : values()) {
            if (channel.getCode().equals(code)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown notification channel code: " + code);
    }
    
    /**
     * Get all currently supported channels
     */
    public static NotificationChannel[] getSupportedChannels() {
        return java.util.Arrays.stream(values())
                .filter(NotificationChannel::isCurrentlySupported)
                .toArray(NotificationChannel[]::new);
    }
}
