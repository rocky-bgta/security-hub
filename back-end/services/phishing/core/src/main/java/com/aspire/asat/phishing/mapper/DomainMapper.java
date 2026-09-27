package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.response.DomainDto;
import com.aspire.asat.phishing.model.Domain;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Domain entity and DTOs.
 */
@Component
public class DomainMapper {
    
    /**
     * Convert Domain entity to DomainDto
     * @param domain The domain entity
     * @param canLock Whether the current tenant can lock domains (based on subscription)
     * @return DomainDto
     */
    public DomainDto toDto(Domain domain, boolean canLock) {
        if (domain == null) {
            return null;
        }
        
        return DomainDto.builder()
                .domainId(domain.getId())
                .domain(domain.getDomain())
                .status(domain.getStatus())
                .isLocked(domain.isLocked())
                .canLock(canLock)
                .lockedByTenantId(domain.getLockedByTenantId())
                .verifiedBy(domain.getVerifiedBy())
                .lockedBy(domain.getLockedBy())
                .isGlobal(domain.isGlobal())
                .createdByRole(domain.getCreatedByRole())
                .verifiedAt(domain.getVerifiedAt())
                .lockedAt(domain.getLockedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert Domain entity to DomainDto with default canLock = false
     * @param domain The domain entity
     * @return DomainDto
     */
    public DomainDto toDto(Domain domain) {
        return toDto(domain, false);
    }
}
