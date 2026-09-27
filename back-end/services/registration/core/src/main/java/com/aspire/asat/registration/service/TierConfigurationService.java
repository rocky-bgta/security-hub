package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.tierManagement.CreateTierConfigurationRequestDTO;
import com.aspire.asat.registration.data.tierManagement.TierConfigurationResponseDTO;
import com.aspire.asat.registration.data.tierManagement.UpdateTierConfigurationRequestDTO;

import java.util.List;

public interface TierConfigurationService {

    TierConfigurationResponseDTO createTier(CreateTierConfigurationRequestDTO dto);

    // Updated method with pagination and filtering
    List<TierConfigurationResponseDTO> getAllTiers(String search, Boolean status, int offset, int limit);

    // For returning the total count with the same filters
    long getTotalTierCount(String search, Boolean status);

    TierConfigurationResponseDTO getTierByMspId(String mspId);

    TierConfigurationResponseDTO getTierById(String id);

    TierConfigurationResponseDTO updateTier(String id, UpdateTierConfigurationRequestDTO dto);

}
