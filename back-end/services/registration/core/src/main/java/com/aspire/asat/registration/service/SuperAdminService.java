package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.superAdmin.response.LicenseHistoryPaginatedResponseDto;
import com.aspire.asat.registration.data.superAdmin.response.MspLicenseHistoryPaginatedResponseDto;

public interface SuperAdminService {

    /**
     * Get license history for all client admins with optional filtering
     *
     * @param clientAdminId Optional filter by client admin ID
     * @param productId     Optional filter by product ID
     * @param packageId     Optional filter by package ID
     * @param country       Optional filter by country / countryId (ClientAdmin)
     * @param mspId         Optional filter by MSP ID (ClientAdmin)
     * @param search        Optional search by client admin organization name
     * @param offset        Page offset (default: 0)
     * @param pageSize      Page size (default: 10)
     * @param sortBy        Sort field (default: assignedAt)
     * @param order         Sort order - asc or desc (default: desc)
     * @return Paginated license history response
     */
    LicenseHistoryPaginatedResponseDto getLicenseHistory(
            String clientAdminId,
            String productId,
            String packageId,
            String country,
            String mspId,
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    );

    /**
     * Get license history for all MSPs with optional filtering
     *
     * @param mspId     Optional filter by MSP ID
     * @param productId Optional filter by product ID
     * @param packageId Optional filter by package ID
     * @param country   Optional filter by country
     * @param search    Optional search by MSP name or email
     * @param offset    Page offset (default: 0)
     * @param pageSize  Page size (default: 10)
     * @param sortBy    Sort field (default: assignedAt)
     * @param order     Sort order - asc or desc (default: desc)
     * @return Paginated MSP license history response
     */
    MspLicenseHistoryPaginatedResponseDto getMspLicenseHistory(
            String mspId,
            String productId,
            String packageId,
            String country,
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    );
}

