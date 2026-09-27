package com.aspire.asat.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for notification system branding and URLs
 */
@Component
@ConfigurationProperties(prefix = "notification.branding")
@Data
public class NotificationBrandingProperties {

    /**
     * Company name for notifications
     */
    private String companyName = "ASAT Learning Platform";

    /**
     * Support email address
     */
    private String supportEmail = "support@securityawarenesstraining.ai";

    /**
     * Login URL for the platform
     */
    private String loginUrl = "https://portal.securityawarenesstraining.ai/auth/login";

    /**
     * Company logo URL
     */
    private String logoUrl = "https://asat-platform.com/logo.png";

    /**
     * Platform base URL
     */
    private String baseUrl = "https://asat-platform.com";

    /**
     * Banner image URL displayed at the top of email templates
     */
    private String bannerUrl = "https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/aspire-soc.png";

    private String platformFeatures="Personalized learning paths\\n- Interactive courses\\n- Certificates on completion";
}
