package com.aspire.asat.notification.service.sms.impl;

import com.aspire.asat.notification.service.sms.SmsProvider;
import com.aspire.asat.notification.util.PhoneNumberParser;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Default SMS provider implementation (fallback) using Twilio
 * Used when no country-specific provider is available
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultSmsProvider implements SmsProvider {

    private final PhoneNumberParser phoneNumberParser;

    @Value("${sms.providers.default.enabled:false}")
    private boolean enabled;

    @Value("${sms.providers.default.account-sid:}")
    private String accountSid;

    @Value("${sms.providers.default.auth-token:}")
    private String authToken;

    @Value("${sms.providers.default.from-number:}")
    private String fromNumber;

    private boolean twilioInitialized = false;

    @PostConstruct
    public void init() {
        if (enabled && accountSid != null && !accountSid.trim().isEmpty() && 
            authToken != null && !authToken.trim().isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                twilioInitialized = true;
                log.info("Twilio initialized successfully for Default SMS provider");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio for Default SMS provider: {}", e.getMessage(), e);
                twilioInitialized = false;
            }
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!isEnabled()) {
            log.warn("Default SMS provider is not enabled");
            return false;
        }

        if (!twilioInitialized) {
            log.error("Twilio is not initialized for Default SMS provider. Check account-sid and auth-token configuration.");
            return false;
        }

        if (fromNumber == null || fromNumber.trim().isEmpty()) {
            log.error("From number is not configured for Default SMS provider");
            return false;
        }

        // Normalize phone numbers to E.164 format
        String normalizedFromNumber = phoneNumberParser.normalizePhoneNumber(fromNumber);
        String normalizedToNumber = phoneNumberParser.normalizePhoneNumber(phoneNumber);
        
        try {
            log.info("Sending SMS via Twilio (Default) to: {} (normalized: {})", phoneNumber, normalizedToNumber);
            log.debug("SMS message: {}", message);

            Message twilioMessage = Message.creator(
                    new PhoneNumber(normalizedToNumber),
                    new PhoneNumber(normalizedFromNumber),
                    message
            ).create();

            Message.Status status = twilioMessage.getStatus();
            String messageSid = twilioMessage.getSid();
            
            log.info("Twilio SMS sent. Status: {}, Message SID: {}", status, messageSid);

            // Check if message was successfully queued or sent
            boolean success = status == Message.Status.QUEUED || 
                            status == Message.Status.SENT ||
                            status == Message.Status.SENDING;
            
            if (success) {
                log.info("SMS sent successfully via Twilio (Default) to: {}", phoneNumber);
            } else {
                log.warn("SMS status is not successful. Status: {}, Message SID: {}", status, messageSid);
            }
            
            return success;
            
        } catch (com.twilio.exception.ApiException e) {
            String errorMessage = e.getMessage();
            if (e.getCode() == 21659) {
                // Phone number doesn't belong to account
                log.error("Twilio API error: Phone number '{}' does not belong to account '{}'. " +
                        "Please check Twilio Console > Phone Numbers > Manage > Active numbers " +
                        "and use a phone number that belongs to this account. Error: {} (Code: {})", 
                        normalizedFromNumber, accountSid, errorMessage, e.getCode());
            } else {
                log.error("Twilio API error sending SMS to {}: {} (Code: {})", 
                        phoneNumber, errorMessage, e.getCode(), e);
            }
            return false;
        } catch (Exception e) {
            log.error("Error sending SMS via Twilio (Default) to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "DefaultSmsProvider";
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
