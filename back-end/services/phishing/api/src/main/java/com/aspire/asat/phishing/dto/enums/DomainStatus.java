package com.aspire.asat.phishing.dto.enums;

/**
 * Status values for domain verification and locking.
 * Based on BRD Use Case 2.1.1.1
 */
public enum DomainStatus {
    /**
     * Domain has not been verified yet
     */
    UNVERIFIED,
    
    /**
     * Domain has been verified via email challenge
     */
    VERIFIED,
    
    /**
     * Domain is verified and locked to a specific tenant
     * (Available only for Professional/Enterprise tiers)
     */
    VERIFIED_AND_LOCKED
}
