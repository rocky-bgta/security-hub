package com.aspire.asat.notification.service;

import com.aspire.asat.notification.service.sms.SmsProvider;
import com.aspire.asat.notification.service.sms.SmsProviderFactory;
import com.aspire.asat.notification.util.PhoneNumberParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for sending SMS notifications
 * Handles phone number parsing, provider selection, and SMS delivery
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final PhoneNumberParser phoneNumberParser;
    private final SmsProviderFactory smsProviderFactory;

    /**
     * Send SMS message to phone number
     * @param phoneNumber Phone number (can be in various formats, will be normalized)
     * @param message SMS message content
     * @return true if SMS sent successfully, false otherwise
     */
    public boolean sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.error("Phone number is null or empty");
            return false;
        }

        if (message == null || message.trim().isEmpty()) {
            log.error("SMS message is null or empty");
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

            // Get appropriate SMS provider
            SmsProvider provider = smsProviderFactory.getProvider(countryCode);
            log.info("Using SMS provider: {} for phone number: {}", provider.getProviderName(), normalizedPhone);

            // Send SMS via provider
            boolean success = provider.sendSms(normalizedPhone, message);
            
            if (success) {
                log.info("SMS sent successfully to: {} via provider: {}", normalizedPhone, provider.getProviderName());
            } else {
                log.error("Failed to send SMS to: {} via provider: {}", normalizedPhone, provider.getProviderName());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }
}
