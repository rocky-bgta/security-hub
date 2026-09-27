package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.clientAdmin.request.*;
import com.aspire.asat.registration.data.clientAdmin.response.BuyNowResponseDto;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientOnboardingResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductDTO;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductReassignmentResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.EmailTemplateValidationResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminListResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminWithProductsResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminDetailedResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductsPaginatedResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductDetailedDto;
import com.aspire.asat.registration.data.clientAdmin.response.LicenseOverviewSummaryDto;
import com.aspire.asat.registration.data.clientAdmin.response.LicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientMspListItemDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientDropdownDto;
import com.aspire.asat.registration.data.clientAdmin.response.OrganizationLicenseStatisticsResponseDto;

import java.util.List;

public interface ClientAdminService {

    ClientOnboardingResponseDto processClientOnboarding(ClientOnboardingRequestDto requestDto);

    BuyNowResponseDto processBuyNow(BuyNowRequestDto requestDto);

    EmailTemplateValidationResponseDto validateTemplate(String templateBody);

    boolean doesUsernameExist(String email);

    void activateLicense(String clientId);

    /**
     * Deactivates licenses for the given client products after coupon-expiry invoice cancellation.
     * Sets ClientAdmin to INACTIVE only when current status is PENDING.
     */
    void deactivateLicense(String clientId, List<String> clientProductIds);

    /**
     * Expires invoice-linked client products after unpaid invoice expiry (PENDING/ACTIVE → EXPIRED).
     * Sets ClientAdmin (and cascaded users) to INACTIVE only when current status is PENDING.
     */
    void expireLicenseDueToUnpaidInvoice(String clientId, List<String> clientProductIds);

    List<String> findProductIdsByClientAdminId(String clientAdminId);

    List<String> findEndUsersByClientAdminId(String clientAdminId);

    List<ClientProductDTO> findAllClientProductsByClientAdminId(String clientAdminId, String productId);

    void updateUsedLicenseCount(String clientAdminId, String productId, int usedLicenseCount);

    /**
     * Sets used license count on a specific ClientProduct assignment by its document id
     * ({@code productPackageId} in phishing). Only ACTIVE products are updated.
     */
    void updateUsedLicenseCountByClientProductId(String clientProductId, int usedLicenseCount);

    ClientProductReassignmentResponseDto reassignProductsToClient(ClientProductAssignment assignmentRequest);

    ClientAdminListResponseDto listClientAdminsWithProducts(ClientAdminListRequestDto requestDto);

    ClientAdminDetailedResponseDto getClientAdminDetailedById(String clientAdminId);

    ClientAdminWithProductsResponseDto updateClientAdminById(String clientAdminId, ClientAdminUpdateRequestDto updateRequestDto);

    ClientProductsPaginatedResponseDto getAssignedClientProducts(String clientAdminId, String search, Integer offset, Integer pageSize, String sortBy, String order);

    ClientProductsPaginatedResponseDto getAssignedActiveAndPendingClientProducts(String clientAdminId, String search, Integer offset, Integer pageSize, String sortBy, String order);

    LicenseStatisticsResponseDto getLicenseStatistics(String clientAdminId);

    LicenseStatisticsResponseDto getLicenseStatistics(String clientAdminId, String productId);

    LicenseOverviewSummaryDto getLicenseOverviewSummary(String clientAdminId);

    ClientProductDetailedDto getProductPackageDetailById(String id);

    int getUniqueProductCountByClientAdminId(String clientAdminId);

    void updateClientDashboardInCmsService(String clientAdminId);

    String getMspIdByClientAdminId(String clientAdminId);

    List<ClientMspListItemDto> getClientsOrMspsByCountry(String country, boolean isClient);

    OrganizationLicenseStatisticsResponseDto getOrganizationLicenseStatistics(String clientAdminId);

    List<ClientDropdownDto> getClientsByMspId(String mspId);

    /**
     * Returns clients for MSP product-assign dropdown, optionally filtered by country, state, and search.
     */
    List<ClientDropdownDto> getClientsDropdownForMsp(String mspId, String country, String state, String search);

    /**
     * Update client admin status and handle associated users.
     * When status is INACTIVE: sets all ACTIVE users under the client admin to INACTIVE with isAdminInactive=true.
     * When status is ACTIVE: sets all users with isAdminInactive=true to ACTIVE with isAdminInactive=false.
     *
     * @param clientAdminId The client admin ID
     * @param status        The new status (ACTIVE or INACTIVE)
     */
    void updateClientAdminStatus(String clientAdminId, AdminStatus status);

    /**
     * Update AspireUser (client admin) status with suspend reason and cascade to associated users.
     * When status is SUSPEND: saves suspend reason and suspends all users under the same client admin where isAdminInactive is false.
     * When status is ACTIVE: reactivates users that were suspended due to admin suspension.
     * Sends email notification to the user.
     *
     * @param userId The AspireUser userId (UUID as String)
     * @param requestDto The request DTO containing status and optional suspend reason
     */
    void updateAspireUserStatus(String userId, UserSuspendRequestDto requestDto);

}
