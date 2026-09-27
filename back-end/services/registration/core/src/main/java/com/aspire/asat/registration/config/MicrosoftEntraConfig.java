package com.aspire.asat.registration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "microsoft.entra")
public class MicrosoftEntraConfig {
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String redirectUri;
    private String scopes;
    private String frontendRedirectUrl;
}

