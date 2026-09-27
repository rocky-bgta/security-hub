package com.aspire.asat.notification.service.phonecall.impl;

import com.aspire.asat.notification.service.phonecall.PhoneCallProvider;
import com.aspire.asat.notification.util.PhoneNumberParser;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Phone Call provider implementation using Twilio Voice API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TwilioPhoneCallProvider implements PhoneCallProvider {

    private final PhoneNumberParser phoneNumberParser;

    @Value("${phone-call.providers.twilio.enabled:false}")
    private boolean enabled;

    @Value("${phone-call.providers.twilio.account-sid:}")
    private String accountSid;

    @Value("${phone-call.providers.twilio.auth-token:}")
    private String authToken;

    @Value("${phone-call.providers.twilio.from-number:}")
    private String fromNumber;

    @Value("${phone-call.providers.twilio.country-codes:+1,+880}")
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
                log.info("Twilio initialized successfully for Phone Call provider");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio for Phone Call provider: {}", e.getMessage(), e);
                twilioInitialized = false;
            }
        }
    }

    @Override
    public boolean makeCall(String phoneNumber, String otpCode) {
        if (!isEnabled()) {
            log.warn("Twilio Phone Call provider is not enabled");
            return false;
        }

        if (!twilioInitialized) {
            log.error("Twilio is not initialized for Phone Call provider");
            return false;
        }

        if (fromNumber == null || fromNumber.trim().isEmpty()) {
            log.error("From number is not configured for Twilio Phone Call provider");
            return false;
        }

        try {
            // Normalize phone numbers to E.164 format
            String normalizedFromNumber = phoneNumberParser.normalizePhoneNumber(fromNumber);
            String normalizedToNumber = phoneNumberParser.normalizePhoneNumber(phoneNumber);
            
            log.info("Making phone call via Twilio to: {} (normalized: {}) for OTP delivery", 
                    phoneNumber, normalizedToNumber);

            // Generate TwiML XML directly (no webhook needed)
            String twimlXml = generateTwiML(otpCode);
            
            // Make the call using Twilio Voice API with TwiML passed directly
            // Twilio accepts TwiML as a URL-encoded string using the twiml:// protocol
            Call call = Call.creator(
                    new PhoneNumber(normalizedToNumber),
                    new PhoneNumber(normalizedFromNumber),
                    new Twiml(twimlXml)
            ).create();

            Call.Status status = call.getStatus();
            String callSid = call.getSid();
            
            log.info("Twilio call initiated. Status: {}, Call SID: {}", status, callSid);

            // Check if call was successfully queued or initiated
            boolean success = status == Call.Status.QUEUED || 
                            status == Call.Status.IN_PROGRESS ||
                            status == Call.Status.RINGING;
            
            if (success) {
                log.info("Phone call initiated successfully via Twilio to: {}", normalizedToNumber);
            } else {
                log.warn("Call status is not successful. Status: {}, Call SID: {}", status, callSid);
            }
            
            return success;
            
        } catch (com.twilio.exception.ApiException e) {
            String errorMessage = e.getMessage();
            if (e.getCode() == 21659) {
                // Phone number doesn't belong to account
                log.error("Twilio API error: Phone number '{}' does not belong to account '{}'. " +
                        "Please check Twilio Console > Phone Numbers > Manage > Active numbers " +
                        "and use a phone number that belongs to this account. Error: {} (Code: {})", 
                        phoneNumberParser.normalizePhoneNumber(fromNumber), accountSid, errorMessage, e.getCode());
            } else {
                log.error("Twilio API error making phone call to {}: {} (Code: {})", 
                        phoneNumber, errorMessage, e.getCode(), e);
            }
            return false;
        } catch (Exception e) {
            log.error("Error making phone call via Twilio to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate TwiML for reading OTP code
     * This can be used by a TwiML endpoint to return the TwiML XML
     * Format: Returns TwiML that speaks the OTP code clearly
     */
    public static String generateTwiML(String otpCode) {
        // Format OTP code with spaces for better pronunciation
        String formattedOtp = String.join(" ", otpCode.split(""));
        
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<Response>\n" +
               "    <Say voice=\"alice\">Your verification code is " + formattedOtp + ". I repeat, your verification code is " + formattedOtp + ".</Say>\n" +
               "</Response>";
    }

    @Override
    public String getProviderName() {
        return "TwilioPhoneCallProvider";
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
