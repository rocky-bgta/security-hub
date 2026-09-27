package com.aspire.asat.notification.service.phonecall;

/**
 * Interface for Phone Call provider implementations
 * Different providers can be used for different countries/regions
 */
public interface PhoneCallProvider {
    
    /**
     * Make automated phone call to deliver OTP code
     * @param phoneNumber Phone number in E.164 format (e.g., +8801712345678)
     * @param otpCode OTP code to be read during the call
     * @return true if call initiated successfully, false otherwise
     */
    boolean makeCall(String phoneNumber, String otpCode);
    
    /**
     * Get provider name for logging/identification
     * @return Provider name (e.g., "TwilioPhoneCallProvider")
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
