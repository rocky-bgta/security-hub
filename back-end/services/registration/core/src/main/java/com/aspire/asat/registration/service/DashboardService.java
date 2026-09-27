package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.dashboard.LicenseDistributionResponseDto;
import com.aspire.asat.registration.data.dashboard.UserStatusCountResponseDto;

/**
 * Service interface for dashboard-related operations
 */
public interface DashboardService {

    /**
     * Get user status counts (Active, Inactive, Suspended) from AspireUser table.
     * When {@code mspId} is resolved (explicit param or MSP caller context), counts are
     * scoped to users whose clientAdminId belongs to that MSP.
     *
     * @param mspId optional MSP ID from the request
     * @return UserStatusCountResponseDto containing counts for each status
     */
    UserStatusCountResponseDto getUserStatusCounts(String mspId);

    /**
     * Get license distribution totals from {@code msp_products}.
     * When {@code mspId} is resolved (explicit param or MSP caller), scoped by that mspId;
     * otherwise platform-wide (Aspire Admin / System User).
     *
     * @param mspId optional MSP ID from the request
     * @return license distribution totals
     */
    LicenseDistributionResponseDto getLicenseDistribution(String mspId);
}
