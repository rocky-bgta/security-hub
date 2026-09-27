package com.aspire.asat.registration.repository.custom;

import com.aspire.asat.registration.data.OrganizationLicenseStatistics;
import com.aspire.asat.registration.data.enums.MspStatus;
import com.aspire.asat.registration.model.msp.MspUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MspUsersRepositoryCustom {

    /**
     * Find MSP users with filtering and pagination
     * @param search Search keyword for organization name, contact email, or MSP admin email
     * @param status Status filter
     * @param pageable Pagination parameters
     * @return Page of MspUser entities
     */
    Page<MspUser> findMspUsersWithFilters(
            String search,
            MspStatus status,
            Pageable pageable
    );

    /**
     * Find MSP users with filtering and pagination (includes mspTier and country filters)
     * @param search Search keyword for organization name only
     * @param status Status filter
     * @param mspTier MSP tier filter
     * @param country Country filter
     * @param pageable Pagination parameters
     * @return Page of MspUser entities
     */
    Page<MspUser> findMspUsersWithFilters(
            String search,
            MspStatus status,
            String mspTier,
            String country,
            Pageable pageable
    );

    /**
     * Get organization dashboard license statistics for a specific MSP
     * Calculates total available, allocated, active, and expired licenses
     * @param mspId the MSP ID
     * @return OrganizationLicenseStatistics containing all license counts
     */
    OrganizationLicenseStatistics getOrganizationLicenseStatisticsByMspId(String mspId);

}

