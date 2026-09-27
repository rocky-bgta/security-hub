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
import java.util.Arrays;
import java.util.List;

/**
 * SMS provider implementation for North America (US, Canada) using Twilio
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NorthAmericaSmsProvider implements SmsProvider {

    private final PhoneNumberParser phoneNumberParser;

    @Value("${sms.providers.north-america.enabled:false}")
    private boolean enabled;

    @Value("${sms.providers.north-america.account-sid:}")
    private String accountSid;

    @Value("${sms.providers.north-america.auth-token:}")
    private String authToken;

    @Value("${sms.providers.north-america.from-number:}")
    private String fromNumber;

    @Value("${sms.providers.north-america.country-codes:+1}")
    private String countryCodes;

    private List<String> supportedCountryCodes;
    private boolean twilioInitialized = false;

    @PostConstruct
    public void init() {
        if (enabled && accountSid != null && !accountSid.trim().isEmpty() && 
            authToken != null && !authToken.trim().isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                twilioInitialized = true;
                log.info("Twilio initialized successfully for North America SMS provider");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio for North America SMS provider: {}", e.getMessage(), e);
                twilioInitialized = false;
            }
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!isEnabled()) {
            log.warn("North America SMS provider is not enabled");
            return false;
        }

        if (!twilioInitialized) {
            log.error("Twilio is not initialized for North America SMS provider. Check account-sid and auth-token configuration.");
            return false;
        }

        if (fromNumber == null || fromNumber.trim().isEmpty()) {
            log.error("From number is not configured for North America SMS provider");
            return false;
        }

        // Normalize phone numbers to E.164 format
        String normalizedFromNumber = phoneNumberParser.normalizePhoneNumber(fromNumber);
        String normalizedToNumber = phoneNumberParser.normalizePhoneNumber(phoneNumber);
        
        try {
            log.info("Sending SMS via Twilio (North America) to: {} (normalized: {})", phoneNumber, normalizedToNumber);
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
                log.info("SMS sent successfully via Twilio (North America) to: {}", phoneNumber);
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
            log.error("Error sending SMS via Twilio (North America) to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "NorthAmericaSmsProvider";
    }

    @Override
    public boolean supportsCountry(String countryCode) {
        if (countryCode == null) {
            return false;
        }
        
        if (supportedCountryCodes == null) {
            // Parse country codes from configuration
            supportedCountryCodes = Arrays.asList(countryCodes.split(","));
        }
        
        return supportedCountryCodes.contains(countryCode.trim());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
