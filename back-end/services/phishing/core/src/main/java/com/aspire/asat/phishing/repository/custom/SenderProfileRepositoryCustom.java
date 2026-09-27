package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import com.aspire.asat.phishing.model.SenderProfile;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Custom queries for sender profile list/count with tenant scope, search, profileType, and isVerified.
 */
public interface SenderProfileRepositoryCustom {

    /**
     * @param tenantClientId when non-null, restricts to clientId match or global profiles; when null, no tenant filter
     */
    List<SenderProfile> findWithFilters(
            String tenantClientId,
            String searchKeyword,
            ProfileType profileType,
            Boolean isVerified,
            String category,
            String targetIndustryId,
            String regionId,
            String language,
            String deceptionLevelId,
            List<String> psychologicalTriggers,
            DomainType domainType,
            String personalizationLevelId,
            ProviderType providerType,
            List<String> tags,
            Pageable pageable);

    long countWithFilters(
            String tenantClientId,
            String searchKeyword,
            ProfileType profileType,
            Boolean isVerified,
            String category,
            String targetIndustryId,
            String regionId,
            String language,
            String deceptionLevelId,
            List<String> psychologicalTriggers,
            DomainType domainType,
            String personalizationLevelId,
            ProviderType providerType,
            List<String> tags);
}
