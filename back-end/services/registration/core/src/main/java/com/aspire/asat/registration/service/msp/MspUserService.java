package com.aspire.asat.registration.service.msp;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.enums.MspStatus;
import com.aspire.asat.registration.data.mspUser.request.MspBuyNowRequestDto;
import com.aspire.asat.registration.data.mspUser.request.MspOnboardingRequestDto;
import com.aspire.asat.registration.data.clientAdmin.response.OrganizationLicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.mspUser.response.*;
import com.aspire.asat.registration.data.mspUser.request.MspUpdateRequestDto;

import java.util.List;

public interface MspUserService {
    MspOnboardingResponseDto processMspOnboarding(MspOnboardingRequestDto requestDto);

    MspBuyNowResponseDto processMspBuyNow(MspBuyNowRequestDto requestDto);

    MspProductCatalogResponseDto getMspProductCatalog(
            String mspId, String search, String mspProductStatus, Integer offset, Integer pageSize);

    AllResponseDto<List<MspAssignedProductResponseDto>> getAssignedMspProducts(
            String mspId, String search, String status, Integer offset, Integer pageSize, String sortBy, String order);

    /**
     * Get unique CMS products assigned to an MSP (mirrors CMS GET /client-product/{clientAdminId}).
     *
     * @param mspId MSP ID
     * @return List of unique products with id, productName, thumbnailUrl, displayOrder from CMS
     */
    List<MspProductSimpleResponseDto> getUniqueProductsByMspId(String mspId);

    /**
     * Get packages assigned to an MSP for a specific product (mirrors CMS GET /client-product/packages).
     * Loads MspProduct rows by mspId + productId, collects unique packageIds, and resolves names from CMS.
     *
     * @param mspId     MSP ID
     * @param productId CMS product ID
     * @return List of packages with id and name
     */
    List<MspPackageSimpleResponseDto> getPackagesByMspIdAndProductId(String mspId, String productId);

    /**
     * Returns package IDs the MSP has purchased for a product (from {@code msp_products}),
     * without enriching names from CMS.
     */
    List<String> getPurchasedPackageIdsByMspIdAndProductId(String mspId, String productId);

    /**
     * Fetch topics for an MSP by loading MspProducts, extracting productId/packageId pairs,
     * and querying CMS topics for those pairs with optional TopicFilterRequest filters.
     */
    AllResponseDto<List<com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto>> getClientProductTopicsByMspId(
            String mspId, com.aspire.asat.registration.data.mspUser.request.MspClientProductTopicsRequestDto request);

    /**
     * Get detailed MSP product-package information by msp_products document id.
     *
     * @param id MSP product id ({@code msp_products.id})
     * @return Detailed product-package assignment including CMS product/package details
     */
    MspProductDetailedDto getMspProductDetailById(String id);
    
    AllResponseDto<List<MspListResponseDto>> getAllMsp(Integer offset, Integer pageSize, String search, MspStatus status);
    
    MspDetailsResponseDto getMspDetails(String mspId);
    MspViewDetailsResponseDto getMspViewDetails(String mspId);

    OrganizationLicenseStatisticsResponseDto getLicenseStatistics(String mspId);

    AllResponseDto<List<MspOnboardedListResponseDto>> getOnboardedMsps(
            Integer offset,
            Integer pageSize,
            String search,
            MspStatus status,
            Integer minLicenses,
            Integer maxLicenses
    );

    /**
     * Update MSP user status
     * @param id MSP user ID
     * @param status New status
     */
    void updateMspStatus(String id, MspStatus status);

    MspUpdateResponseDto updateMsp(String mspId, MspUpdateRequestDto requestDto);

    /**
     * Update MSP user status from AspireUser and cascade to associated client admins and users.
     * When status is INACTIVE: deactivates client admins with PENDING products and their users.
     * When status is ACTIVE: reactivates previously deactivated users.
     *
     * @param userId The user ID from AspireUser (UUID as String)
     * @param status The new status (ACTIVE or INACTIVE)
     */
    void updateMspUserStatus(String userId, MspStatus status);

    /**
     * Suspend or activate MSP user (AspireUser) with suspend reason and cascade to associated users.
     * When status is SUSPEND: suspends all users under the MSP's client admins.
     * When status is ACTIVE: reactivates previously suspended users.
     *
     * @param userId The user ID from AspireUser (UUID as String)
     * @param requestDto The request DTO containing status (SUSPEND or ACTIVE) and optional suspend reason
     */
    void suspendMspUser(String userId, UserSuspendRequestDto requestDto);

    /**
     * Activate MSP license by updating MspUser status to ACTIVE, MspProduct licenseStatus to ACTIVE,
     * and MspInvoice status to PAID.
     *
     * @param mspId The MSP ID (same as MspUser.id)
     * @param invoiceId Optional billing invoice ID; when provided, only products on that invoice are activated
     */
    void activateLicense(String mspId, String invoiceId);

    default void activateLicense(String mspId) {
        activateLicense(mspId, null);
    }

    /**
     * Get assigned clients for MSP with pagination.
     *
     * @param mspId The MSP ID
     * @param offset Page offset (default: 0)
     * @param pageSize Page size (default: 10)
     * @return List of assigned clients
     */
    List<com.aspire.asat.registration.data.mspUser.response.AssignedClientDto> getAssignedClientsForMsp(
            String mspId, Integer offset, Integer pageSize);

    /**
     * Count total assigned clients for MSP.
     *
     * @param mspId The MSP ID
     * @return Total count of assigned clients
     */
    long countAssignedClientsForMsp(String mspId);

    /**
     * Get MSP list with aggregated license counts
     *
     * @param offset Page offset (default: 0)
     * @param pageSize Page size (default: 10)
     * @param search Search by organization name
     * @param status Filter by status
     * @param mspTier Filter by MSP tier
     * @param country Filter by country
     * @return Paginated list of MSPs with license counts
     */
    AllResponseDto<List<com.aspire.asat.registration.data.mspUser.response.MspListWithLicenseResponseDto>> getMspListWithLicenses(
            Integer offset,
            Integer pageSize,
            String search,
            MspStatus status,
            String mspTier,
            String country
    );

    /**
     * Get total active license summary across all MSPs
     * Returns aggregated licenseCount and usedLicenseCount for all products with ACTIVE licenseStatus
     *
     * @return MspActiveLicenseSummaryResponseDto with total active license counts
     */
    com.aspire.asat.registration.data.mspUser.response.MspActiveLicenseSummaryResponseDto getActiveLicenseSummary();

    /**
     * Get MSP dashboard totals: unique products, total licenses, unique packages, and client count.
     *
     * @param mspId MSP ID
     * @return MspDashboardResponseDto with dashboard totals
     */
    MspDashboardResponseDto getMspDashboard(String mspId);

    /**
     * Get MSP topic monthly distribution for the current year:
     * totalContent from msp_products pairs, usedContent from client_products pairs,
     * resolved via CMS topic.productPackageMappings (same shape as /topics/distribution).
     *
     * @param mspId MSP ID
     * @return MspTopicCountsResponseDto with 12 monthly items
     */
    MspTopicCountsResponseDto getMspTopicCounts(String mspId);

}
