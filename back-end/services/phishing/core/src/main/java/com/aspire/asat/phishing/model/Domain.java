package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.DomainStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Domain entity for domain verification and locking.
 * Based on BRD Use Case 2.1.1.1: Aspire Admin Verifies and Locks a Domain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "domains")
public class Domain {
    
    @Id
    private String id;
    
    /**
     * Multi-tenant identifier - obtained from UserCurrentContextService
     */
    @Indexed
    private String clientId;
    
    /**
     * Domain name (e.g., "aspiretech.com")
     * Extracted from email address during verification
     */
    @Indexed(unique = true)
    private String domain;
    
    /**
     * Current status of the domain
     */
    private DomainStatus status;
    
    /**
     * Whether the domain is locked (prevents other tenants from verifying)
     */
    private boolean isLocked;
    
    /**
     * Tenant ID that locked this domain (if locked)
     */
    private String lockedByTenantId;
    
    /**
     * User ID who verified this domain
     */
    private String verifiedBy;
    
    /**
     * User ID who locked this domain
     */
    private String lockedBy;
    
    /**
     * Timestamp when the domain was verified
     */
    private Instant verifiedAt;
    
    /**
     * Timestamp when the domain was locked
     */
    private Instant lockedAt;
    
    // Audit fields
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;

    @Builder.Default
    private boolean isGlobal = false;

    private String createdByRole;

    private String lastModifiedBy;
}
