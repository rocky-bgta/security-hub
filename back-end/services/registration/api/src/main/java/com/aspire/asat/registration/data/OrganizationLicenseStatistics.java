package com.aspire.asat.registration.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Organization dashboard license statistics data class
 * Contains calculated license statistics for organization dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationLicenseStatistics {
    private int totalAvailableLicenses;
    private int totalAllocatedLicenses;
    private int totalActiveLicenses;
    private int totalExpiredLicenses;
}

