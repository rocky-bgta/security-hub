package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.AdminDomainAddRequest;
import com.aspire.asat.phishing.dto.request.DomainVerificationRequest;
import com.aspire.asat.phishing.dto.request.GenerateVerificationRequest;
import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.dto.response.DomainDto;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for domain verification and locking operations.
 * Based on BRD Use Case 2.1.1.1
 */
public interface DomainService {
    
    /**
     * Get all domains for the current tenant with pagination and search
     * @param search Optional search term for domain name
     * @param status Optional filter by one or more domain statuses
     * @param offset Page offset
     * @param pageSize Number of items per page
     * @param sortBy Field to sort by
     * @param sortDirection Sort direction (asc/desc)
     * @return List of DomainDto
     */
    List<DomainDto> getDomains(String search, List<DomainStatus> status, int offset, int pageSize, 
                               String sortBy, String sortDirection);
    
    /**
     * Count domains matching search criteria
     * @param search Optional search term
     * @param status Optional filter by one or more domain statuses
     * @return Total count
     */
    long countDomains(String search, List<DomainStatus> status);
    
    /**
     * Get a domain by its ID
     * @param domainId Domain ID
     * @return Optional DomainDto
     */
    Optional<DomainDto> getDomainById(String domainId);
    
    /**
     * Generate and send a verification email with OTP code
     * BR-06: Codes expire after 15 minutes
     * BR-07: Max 5 requests per domain per hour
     * @param request Contains the email address
     */
    void generateVerificationEmail(GenerateVerificationRequest request);
    
    /**
     * Verify a domain using the OTP code
     * BR-01: Email must match the domain
     * BR-06: Code is one-time use
     * @param request Contains email and verification code
     * @return Verified DomainDto
     */
    DomainDto verifyDomain(DomainVerificationRequest request);

    /**
     * Aspire admin: create a domain as {@code VERIFIED}, or set an existing domain to {@code VERIFIED}.
     * Clears lock state when updating from a locked status so the record stays consistent.
     */
    DomainDto adminAddVerifiedDomain(AdminDomainAddRequest request);
    
    /**
     * Lock a verified domain to the current tenant
     * BR-02: Only available for Professional/Enterprise tiers
     * BR-03: Locked domain cannot be re-verified by others
     * @param domainId Domain ID to lock
     * @return Locked DomainDto
     */
    DomainDto lockDomain(String domainId);
    
    /**
     * Unlock a previously locked domain
     * @param domainId Domain ID to unlock
     * @return Unlocked DomainDto
     */
    DomainDto unlockDomain(String domainId);
    
    /**
     * Delete a domain
     * BR-08: Admin can only delete domains they created
     * @param domainId Domain ID to delete
     */
    void deleteDomain(String domainId);
    
    /**
     * Resend verification email
     * @param request Contains the email address
     */
    void resendVerificationEmail(GenerateVerificationRequest request);
    
    /**
     * Check if the current tenant can lock domains
     * Based on subscription tier (Professional/Enterprise)
     * @return true if locking is available
     */
    boolean canLockDomains();
}
