package com.aspire.asat.registration.repository.custom;

import com.aspire.asat.registration.data.OrganizationLicenseStatistics;
import com.aspire.asat.registration.model.ClientProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface ClientProductRepositoryCustom {

    /**
     * Find client products by client admin ID with pagination, search, and sorting
     * @param clientAdminId the client admin ID
     * @param search the search term (searches in productId, packageId, licenseStatus)
     * @param pageable pagination and sorting information
     * @return Page of ClientProduct
     */
    Page<ClientProduct> findClientProductsByClientAdminIdWithFilters(String clientAdminId, String search, Pageable pageable);

    /**
     * Get license statistics for a specific client admin, optionally filtered by product.
     * @param clientAdminId the client admin ID
     * @param productId optional product ID; when null/blank, aggregates across all products
     * @return LicenseStatistics containing total and used license counts
     */
    LicenseStatistics getLicenseStatisticsByClientAdminId(String clientAdminId, String productId);

    /**
     * Get license statistics for a specific client admin across all products.
     */
    default LicenseStatistics getLicenseStatisticsByClientAdminId(String clientAdminId) {
        return getLicenseStatisticsByClientAdminId(clientAdminId, null);
    }

    /**
     * Get unique product count for a specific client admin
     * @param clientAdminId the client admin ID
     * @return count of unique products
     */
    int getUniqueProductCountByClientAdminId(String clientAdminId);

    /**
     * Get organization dashboard license statistics for a specific client admin
     * Calculates total available, allocated, active, and expired licenses
     * @param clientAdminId the client admin ID
     * @return OrganizationLicenseStatistics containing all license counts
     */
    OrganizationLicenseStatistics getOrganizationLicenseStatisticsByClientAdminId(String clientAdminId);

    /**
     * Find all client products with optional filters.
     * Always aggregates ClientProduct with ClientAdmin; filters by clientAdminId, productId, packageId,
     * country (ClientAdmin), mspId (ClientAdmin), and optional search by organization name.
     *
     * @param clientAdminId Optional filter by client admin ID
     * @param productId     Optional filter by product ID
     * @param packageId     Optional filter by package ID
     * @param country       Optional filter by country / countryId (matches ClientAdmin country or countryCode)
     * @param mspId         Optional filter by MSP ID (from ClientAdmin)
     * @param search        Optional search by client admin organization name (case-insensitive)
     * @return List of ClientProduct matching the filters
     */
    java.util.List<ClientProduct> findAllWithFilters(String clientAdminId, String productId, String packageId, String country, String mspId, String search);

    /**
     * Fetch client products for the Subscription Summary Report, narrowed by the
     * DB-resolvable filters. Name-based search, status derivation and pagination
     * are applied in the service layer because product/package names live in the
     * CMS service.
     *
     * @param clientAdminId       optional exact client admin ID filter
     * @param mspId               optional exact MSP ID filter
     * @param assignedFrom        optional inclusive lower bound on {@code assignedAt}
     * @param assignedToExclusive optional exclusive upper bound on {@code assignedAt}
     * @return matching client products sorted by {@code assignedAt} descending
     */
    java.util.List<ClientProduct> findSubscriptionsForReport(String clientAdminId, String mspId, Instant assignedFrom, Instant assignedToExclusive);

    /**
     * License statistics data class
     */
    class LicenseStatistics {
        private int totalLicenseCount;
        private int totalUsedLicenseCount;

        public LicenseStatistics() {}

        public LicenseStatistics(int totalLicenseCount, int totalUsedLicenseCount) {
            this.totalLicenseCount = totalLicenseCount;
            this.totalUsedLicenseCount = totalUsedLicenseCount;
        }

        public int getTotalLicenseCount() {
            return totalLicenseCount;
        }

        public void setTotalLicenseCount(int totalLicenseCount) {
            this.totalLicenseCount = totalLicenseCount;
        }

        public int getTotalUsedLicenseCount() {
            return totalUsedLicenseCount;
        }

        public void setTotalUsedLicenseCount(int totalUsedLicenseCount) {
            this.totalUsedLicenseCount = totalUsedLicenseCount;
        }
    }
}
