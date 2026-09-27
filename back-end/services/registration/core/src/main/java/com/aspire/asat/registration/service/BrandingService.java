package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.branding.request.BrandingCreateRequestDTO;
import com.aspire.asat.registration.data.branding.request.BrandingUpdateRequestDTO;
import com.aspire.asat.registration.data.branding.response.BrandingResponseDTO;

public interface BrandingService {

    /**
     * Create branding from DTO
     */
    BrandingResponseDTO createBranding(BrandingCreateRequestDTO requestDTO);

    /**
     * Update branding
     */
    BrandingResponseDTO updateBranding(BrandingUpdateRequestDTO requestDTO);

    /**
     * Soft delete branding by setting active to false
     */
    void deleteBranding(String brandingId);

    /**
     * Get branding by Client Admin ID
     */
    BrandingResponseDTO getBranding();

}
