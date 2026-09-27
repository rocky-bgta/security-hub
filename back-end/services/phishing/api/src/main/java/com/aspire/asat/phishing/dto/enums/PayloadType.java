package com.aspire.asat.phishing.dto.enums;

/**
 * Types of payload/attack vectors for phishing templates.
 * Based on BRD Use Case 2.1.3
 */
public enum PayloadType {
    /**
     * Phishing link to a fake website
     */
    PHISHING_WEBSITE,
    
    /**
     * Malicious attachment
     */
    PHISHING_ATTACHMENT,
    
    /**
     * QR code phishing
     */
    QR,
    
    /**
     * Information request via reply
     */
    INFORMATION_REQUEST,
    
    /**
     * Request for callback/phone call
     */
    CALLBACK_REQUEST,
    
    /**
     * Social media impersonation
     */
    SOCIAL_MEDIA_PHISHING,
    
    /**
     * Fake software update
     */
    FAKE_SOFTWARE_UPDATE,
    
    /**
     * Fake payment/invoice request
     */
    FAKE_PAYMENT_REQUEST
}
