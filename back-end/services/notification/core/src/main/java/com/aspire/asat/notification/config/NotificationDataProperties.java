package com.aspire.asat.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for notification data initialization
 */
@Component
@ConfigurationProperties(prefix = "notification.data")
@Data
public class NotificationDataProperties {

    /**
     * Whether to initialize data on application startup
     */
    private boolean initializeOnStartup = true;

    /**
     * Whether to skip initialization if data already exists
     */
    private boolean skipIfDataExists = true;
}
