package com.aspire.asat.notification.service.sms;

import com.aspire.asat.notification.service.sms.impl.BangladeshSmsProvider;
import com.aspire.asat.notification.service.sms.impl.DefaultSmsProvider;
import com.aspire.asat.notification.service.sms.impl.NorthAmericaSmsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for selecting appropriate SMS provider based on country code
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsProviderFactory {

    private final List<SmsProvider> smsProviders;
    private final BangladeshSmsProvider bangladeshSmsProvider;
    private final NorthAmericaSmsProvider northAmericaSmsProvider;
    private final DefaultSmsProvider defaultSmsProvider;

    /**
     * Get SMS provider for the given country code
     * Priority: Country-specific providers first, then default provider
     * @param countryCode Country code (e.g., "+880", "+1")
     * @return SMS provider instance, or default provider if no match found
     */
    public SmsProvider getProvider(String countryCode) {
        if (countryCode == null) {
            log.warn("Country code is null, using default SMS provider");
            return defaultSmsProvider;
        }

        // First, try to find a country-specific provider (excluding default)
        for (SmsProvider provider : smsProviders) {
            // Skip default provider - we'll use it as fallback only
            if (provider == defaultSmsProvider || 
                provider.getProviderName().equals("DefaultSmsProvider")) {
                continue;
            }
            
            if (provider.isEnabled() && provider.supportsCountry(countryCode)) {
                log.debug("Selected country-specific SMS provider: {} for country code: {}", 
                        provider.getProviderName(), countryCode);
                return provider;
            }
        }

        // Fallback to default provider only if no country-specific provider found
        if (defaultSmsProvider.isEnabled()) {
            log.debug("No country-specific SMS provider found for country code: {}, using default provider", countryCode);
            return defaultSmsProvider;
        }

        log.warn("No SMS provider found for country code: {} (default provider is disabled)", countryCode);
        return defaultSmsProvider;
    }

    /**
     * Get SMS provider by name (for testing/debugging)
     * @param providerName Provider name
     * @return SMS provider instance or null if not found
     */
    public SmsProvider getProviderByName(String providerName) {
        for (SmsProvider provider : smsProviders) {
            if (provider.getProviderName().equals(providerName)) {
                return provider;
            }
        }
        return null;
    }
}
