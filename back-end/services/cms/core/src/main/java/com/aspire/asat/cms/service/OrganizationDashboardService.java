package com.aspire.asat.cms.service;

import com.aspire.asat.cms.model.OrganizationDashboard;

/**
 * Service interface for Organization Dashboard operations
 * This dashboard represents system-wide statistics for super admin
 * There is only ONE entry in the collection, identified by organizationAdminId from context
 */
public interface OrganizationDashboardService {

    /**
     * Increment total product count
     * Creates dashboard if it doesn't exist
     * @param organizationAdminId the organization admin ID from user context
     * @param createdBy the user who created the product
     */
    void incrementTotalProduct(String organizationAdminId, String createdBy);

    /**
     * Increment total package count
     * Creates dashboard if it doesn't exist
     * @param organizationAdminId the organization admin ID from user context
     * @param packageCount the number of packages to add
     * @param createdBy the user who created the packages
     */
    void incrementTotalPackage(String organizationAdminId, int packageCount, String createdBy);

    /**
     * Get the organization dashboard (super admin dashboard)
     * @param organizationAdminId the organization admin ID from user context
     * @return OrganizationDashboard
     */
    OrganizationDashboard getOrganizationDashboard(String organizationAdminId);

    /**
     * Get consolidated organization dashboard with summed totals across all organization dashboards.
     * Sums totalProduct, totalPackage, totalLicense, totalClient, totalMsp from all records.
     * @return OrganizationDashboard with summed values (id and organizationAdminId are null)
     */
    OrganizationDashboard getConsolidatedOrganizationDashboard();

    /**
     * Create or update organization dashboard
     * @param organizationAdminId the organization admin ID from user context
     * @param createdBy the user who is creating/updating
     * @return OrganizationDashboard
     */
    OrganizationDashboard createOrUpdateDashboard(String organizationAdminId, String createdBy);

    /**
     * Update both total license and total client counts incrementally
     * This is the most efficient method for incremental updates
     * @param organizationAdminId the organization admin ID from user context
     * @param licenseDelta the change in license count (new - old)
     * @param isNewClient true if this is a new client dashboard, false if updating existing
     * @param updatedBy the user who is updating
     */
    void incrementLicenseAndClientCounts(String organizationAdminId, int licenseDelta, boolean isNewClient, String updatedBy);
}

