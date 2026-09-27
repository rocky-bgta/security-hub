package com.aspire.asat.notification.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum for client notification settings action types
 * Defines the available actions that can be performed on client notification settings
 */
public enum ClientNotificationActionType {
    /**
     * Enable a notification type for the client
     */
    ENABLE,
    
    /**
     * Disable a notification type for the client
     */
    DISABLE,
    
    /**
     * Update channel-specific settings for a notification type
     */
    UPDATE_CHANNELS,
    
    /**
     * Delete client-specific notification settings
     */
    DELETE,
    
    /**
     * Initialize default client notification settings based on global defaults
     */
    INITIALIZE;
    
    /**
     * Case-insensitive JSON deserialization support
     * Allows API consumers to send action values in any case
     */
    @JsonCreator
    public static ClientNotificationActionType fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid action: " + value + ". Valid actions are: ENABLE, DISABLE, UPDATE_CHANNELS, DELETE, INITIALIZE");
        }
    }
    
    /**
     * JSON serialization support
     */
    @JsonValue
    public String toValue() {
        return name();
    }
}

