package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.DomainStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for domain information.
 * Based on BRD Use Case 2.1.1.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainDto {
    
    private String domainId;
    
    private String domain;
    
    private DomainStatus status;
    
    private boolean isLocked;
    
    /**
     * Whether the current tenant can lock this domain
     * (Based on subscription tier: Professional/Enterprise only)
     */
    private boolean canLock;
    
    private String lockedByTenantId;
    
    private String verifiedBy;
    
    private String lockedBy;

    private boolean isGlobal;

    private String createdByRole;
    
    private Instant verifiedAt;
    
    private Instant lockedAt;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}
