package com.aspire.asat.registration.service.msp;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.mspUser.response.ClientLicenseSummaryDto;
import com.aspire.asat.registration.data.mspUser.response.ClientLicenseUsageResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspClientLicenseDetailResponseDto;

import java.util.List;

/**
 * Service for MSP Admin to manage and view client (Client Admin) license allocations.
 * Covers list, detail, usage tracking, and export (no allocate/remove in this scope).
 */
public interface MspClientLicenseService {

    /**
     * Get paginated list of Client Admins for the given MSP with license summary.
     */
    AllResponseDto<List<ClientLicenseSummaryDto>> getClientLicenseList(
            String mspId,
            Integer offset,
            Integer pageSize,
            String search);

    /**
     * Get detailed license information for a single client (Client Admin).
     * Validates that the client belongs to the given MSP.
     */
    MspClientLicenseDetailResponseDto getClientLicenseDetail(String mspId, String clientAdminId);

    /**
     * Get license usage for a client: in use, history, inactive count, upcoming expirations.
     * Validates that the client belongs to the given MSP.
     */
    ClientLicenseUsageResponseDto getClientLicenseUsage(String mspId, String clientAdminId);

    /**
     * Export client license data for the MSP in the given format (csv, xlsx, pdf).
     */
    byte[] exportClientLicenses(String mspId, String format);
}
