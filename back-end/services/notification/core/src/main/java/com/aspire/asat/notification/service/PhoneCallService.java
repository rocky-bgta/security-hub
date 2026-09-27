package com.aspire.asat.notification.service;

import com.aspire.asat.notification.service.phonecall.PhoneCallProvider;
import com.aspire.asat.notification.service.phonecall.PhoneCallProviderFactory;
import com.aspire.asat.notification.util.PhoneNumberParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for making phone calls to deliver OTP codes
 * Handles phone number parsing, provider selection, and phone call delivery
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneCallService {

    private final PhoneNumberParser phoneNumberParser;
    private final PhoneCallProviderFactory phoneCallProviderFactory;

    /**
     * Make automated phone call to deliver OTP code
     * @param phoneNumber Phone number (can be in various formats, will be normalized)
     * @param otpCode OTP code to be read during the call
     * @return true if call initiated successfully, false otherwise
     */
    public boolean makeCall(String phoneNumber, String otpCode) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.error("Phone number is null or empty");
            return false;
        }

        if (otpCode == null || otpCode.trim().isEmpty()) {
            log.error("OTP code is null or empty");
            return false;
        }

        try {
            // Normalize phone number
            String normalizedPhone = phoneNumberParser.normalizePhoneNumber(phoneNumber);
            
            // Validate phone number format
            if (!phoneNumberParser.isValidPhoneNumber(normalizedPhone)) {
                log.error("Invalid phone number format: {}", phoneNumber);
                return false;
            }

            // Extract country code
            String countryCode = phoneNumberParser.extractCountryCode(normalizedPhone);
            log.debug("Extracted country code: {} from phone number: {}", countryCode, normalizedPhone);

            // Get appropriate Phone Call provider
            PhoneCallProvider provider = phoneCallProviderFactory.getProvider(countryCode);
            log.info("Using Phone Call provider: {} for phone number: {}", provider.getProviderName(), normalizedPhone);

            // Make phone call via provider
            boolean success = provider.makeCall(normalizedPhone, otpCode);
            
            if (success) {
                log.info("Phone call initiated successfully to: {} via provider: {}", normalizedPhone, provider.getProviderName());
            } else {
                log.error("Failed to initiate phone call to: {} via provider: {}", normalizedPhone, provider.getProviderName());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error making phone call to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }
}
