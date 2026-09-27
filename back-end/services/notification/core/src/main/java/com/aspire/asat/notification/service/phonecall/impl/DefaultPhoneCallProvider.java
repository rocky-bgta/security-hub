package com.aspire.asat.notification.service.phonecall.impl;

import com.aspire.asat.notification.service.phonecall.PhoneCallProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Default Phone Call provider implementation (fallback)
 * Used when no country-specific provider is available
 * This is a placeholder implementation - replace with actual provider integration
 */
@Slf4j
@Component
public class DefaultPhoneCallProvider implements PhoneCallProvider {

    @Value("${phone-call.providers.default.enabled:false}")
    private boolean enabled;

    @Value("${phone-call.providers.default.api-key:}")
    private String apiKey;

    @Value("${phone-call.providers.default.api-secret:}")
    private String apiSecret;

    @Value("${phone-call.providers.default.endpoint:}")
    private String endpoint;

    @Override
    public boolean makeCall(String phoneNumber, String otpCode) {
        if (!isEnabled()) {
            log.warn("Default Phone Call provider is not enabled");
            return false;
        }

        try {
            log.info("Making phone call via Default provider to: {} for OTP delivery", phoneNumber);

            // TODO: Implement actual phone call logic using the configured provider
            // This can be a generic phone call provider that works globally
            
            // Placeholder implementation
            // Replace this with actual API call
            boolean success = makeCallToProvider(phoneNumber, otpCode);
            
            if (success) {
                log.info("Phone call initiated successfully via Default provider to: {}", phoneNumber);
            } else {
                log.error("Failed to initiate phone call via Default provider to: {}", phoneNumber);
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error making phone call via Default provider to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Placeholder method for actual phone call
     * Replace with real provider integration
     */
    private boolean makeCallToProvider(String phoneNumber, String otpCode) {
        // TODO: Implement actual phone call
        // This can use a global phone call provider
        
        // For now, return true to allow testing
        log.warn("Default Phone Call provider - placeholder implementation, call not actually made");
        return true;
    }

    @Override
    public String getProviderName() {
        return "DefaultPhoneCallProvider";
    }

    @Override
    public boolean supportsCountry(String countryCode) {
        // Default provider supports all countries (as fallback)
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
