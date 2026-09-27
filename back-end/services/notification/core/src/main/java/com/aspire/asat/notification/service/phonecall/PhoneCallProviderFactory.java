package com.aspire.asat.notification.service.phonecall;

import com.aspire.asat.notification.service.phonecall.impl.DefaultPhoneCallProvider;
import com.aspire.asat.notification.service.phonecall.impl.TwilioPhoneCallProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for selecting appropriate Phone Call provider based on country code
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhoneCallProviderFactory {

    private final List<PhoneCallProvider> phoneCallProviders;
    private final TwilioPhoneCallProvider twilioPhoneCallProvider;
    private final DefaultPhoneCallProvider defaultPhoneCallProvider;

    /**
     * Get Phone Call provider for the given country code
     * Priority: Country-specific providers first, then default provider
     * @param countryCode Country code (e.g., "+880", "+1")
     * @return Phone Call provider instance, or default provider if no match found
     */
    public PhoneCallProvider getProvider(String countryCode) {
//        if (countryCode == null) {
//            log.warn("Country code is null, using default Phone Call provider");
//            return defaultPhoneCallProvider;
//        }
//
//        // First, try to find a country-specific provider (excluding default)
//        for (PhoneCallProvider provider : phoneCallProviders) {
//            // Skip default provider - we'll use it as fallback only
//            if (provider == defaultPhoneCallProvider ||
//                provider.getProviderName().equals("DefaultPhoneCallProvider")) {
//                continue;
//            }
//
//            if (provider.isEnabled() && provider.supportsCountry(countryCode)) {
//                log.debug("Selected country-specific Phone Call provider: {} for country code: {}",
//                        provider.getProviderName(), countryCode);
//                return provider;
//            }
//        }
//
//        // Fallback to default provider only if no country-specific provider found
//        if (defaultPhoneCallProvider.isEnabled()) {
//            log.debug("No country-specific Phone Call provider found for country code: {}, using default provider", countryCode);
//            return defaultPhoneCallProvider;
//        }
//
//        log.warn("No Phone Call provider found for country code: {} (default provider is disabled)", countryCode);
        return twilioPhoneCallProvider;
    }

    /**
     * Get Phone Call provider by name (for testing/debugging)
     * @param providerName Provider name
     * @return Phone Call provider instance or null if not found
     */
    public PhoneCallProvider getProviderByName(String providerName) {
        for (PhoneCallProvider provider : phoneCallProviders) {
            if (provider.getProviderName().equals(providerName)) {
                return provider;
            }
        }
        return null;
    }
}
