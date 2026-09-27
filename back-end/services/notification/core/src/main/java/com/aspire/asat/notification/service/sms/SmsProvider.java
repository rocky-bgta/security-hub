package com.aspire.asat.notification.service.sms;

/**
 * Interface for SMS provider implementations
 * Different providers can be used for different countries/regions
 */
public interface SmsProvider {
    
    /**
     * Send SMS message to phone number
     * @param phoneNumber Phone number in E.164 format (e.g., +8801712345678)
     * @param message SMS message content
     * @return true if SMS sent successfully, false otherwise
     */
    boolean sendSms(String phoneNumber, String message);
    
    /**
     * Get provider name for logging/identification
     * @return Provider name (e.g., "BangladeshSmsProvider", "TwilioSmsProvider")
     */
    String getProviderName();
    
    /**
     * Check if this provider supports the given country code
     * @param countryCode Country code (e.g., "+880", "+1")
     * @return true if provider supports this country, false otherwise
     */
    boolean supportsCountry(String countryCode);
    
    /**
     * Check if provider is enabled
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();
}
