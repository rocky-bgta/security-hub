package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.requiredinfo.RequiredInfoResponseDTO;

/**
 * Service interface for required info operations
 */
public interface RequiredInfoService {
    /**
     * Get required info status for the logged-in user
     * @return RequiredInfoResponseDTO containing branding, user, and product assignment status
     */
    RequiredInfoResponseDTO getRequiredInfo();
}

