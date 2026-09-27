package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.tierManagement.CreateTierConfigurationRequestDTO;
import com.aspire.asat.registration.data.tierManagement.TierConfigurationResponseDTO;
import com.aspire.asat.registration.data.tierManagement.UpdateTierConfigurationRequestDTO;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.TierConfiguration;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.TierConfigurationRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.TierConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TierConfigurationServiceImpl implements TierConfigurationService {

    private final TierConfigurationRepository repository;
    private final MspUsersRepository mspUsersRepository;

    @Override
    public TierConfigurationResponseDTO createTier(CreateTierConfigurationRequestDTO dto) {
        if (repository.existsByTierNameIgnoreCase(dto.getTierName())) {
            throw new IllegalArgumentException("Tier name already exists");
        }

        String tierId = UUID.randomUUID().toString();

        TierConfiguration entity = TierConfiguration.builder()
                .id(tierId)
                .tierName(dto.getTierName())
                .commissionPercentage(dto.getCommissionPercentage())
                .salesThreshold(dto.getSalesThreshold())
                .active(dto.getActive())
                .eligibilityCriteria(dto.getEligibilityCriteria())
                .tierBenefits(dto.getTierBenefits())
                .tierDescription(dto.getTierDescription())
                .createdAt(Instant.now())
                .build();

        repository.save(entity);
        return mapToDTO(entity);
    }

    @Override
    public List<TierConfigurationResponseDTO> getAllTiers(String search, Boolean status, int offset, int limit) {
        // Basic pagination and filtering using Java logic
        int skipSize = offset == 0 ? 0 : (offset * limit);
        return repository.findAll().stream()
                .filter(t -> (search == null || t.getTierName().toLowerCase().contains(search.toLowerCase())))
                .filter(t -> (status == null || t.isActive() == status))
                .sorted((t1, t2) -> {
                    if (t1.getCreatedAt() == null && t2.getCreatedAt() == null) return 0;
                    if (t1.getCreatedAt() == null) return 1;
                    if (t2.getCreatedAt() == null) return -1;
                    return t2.getCreatedAt().compareTo(t1.getCreatedAt()); // DESC order
                })
                .skip(skipSize)
                .limit(limit)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public long getTotalTierCount(String search, Boolean status) {
        return repository.findAll().stream()
                .filter(t -> (search == null || t.getTierName().toLowerCase().contains(search.toLowerCase())))
                .filter(t -> (status == null || t.isActive() == status))
                .count();
    }

    private TierConfigurationResponseDTO mapToDTO(TierConfiguration config) {
        return TierConfigurationResponseDTO.builder()
                .id(config.getId())
                .tierName(config.getTierName())
                .commissionPercentage(config.getCommissionPercentage())
                .salesThreshold(config.getSalesThreshold())
                .active(config.isActive())
                .eligibilityCriteria(config.getEligibilityCriteria())
                .tierBenefits(config.getTierBenefits())
                .tierDescription(config.getTierDescription())
                .createdAt(config.getCreatedAt())
                .build();
    }

    @Override
    public TierConfigurationResponseDTO getTierByMspId(String mspId) {
        // Fetch MspUser by mspId
        MspUser mspUser = mspUsersRepository.findById(mspId)
                .orElseThrow(() -> new RegistrationServiceException("MSP not found with ID: " + mspId));

        if (mspUser.getMspTier() == null || mspUser.getMspTier().isBlank()) {
            throw new RegistrationServiceException("Tier ID not found for MSP: " + mspId);
        }

        TierConfiguration tier = repository.findById(mspUser.getMspTier())
                .orElseThrow(() -> new RegistrationServiceException("Tier configuration not found with ID: " + mspUser.getMspTier()));

        return mapToDTO(tier);
    }

    @Override
    public TierConfigurationResponseDTO getTierById(String id) {
        TierConfiguration tier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tier configuration not found with ID: " + id));
        return mapToDTO(tier);
    }

    @Override
    public TierConfigurationResponseDTO updateTier(String id, UpdateTierConfigurationRequestDTO dto) {
        TierConfiguration tier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tier configuration not found with ID: " + id));

        // Check if tier name is being changed and if the new name already exists
        if (!tier.getTierName().equalsIgnoreCase(dto.getTierName()) &&
                repository.existsByTierNameIgnoreCase(dto.getTierName())) {
            throw new IllegalArgumentException("Tier name already exists: " + dto.getTierName());
        }

        // Update fields
        tier.setTierName(dto.getTierName());
        tier.setCommissionPercentage(dto.getCommissionPercentage());
        tier.setSalesThreshold(dto.getSalesThreshold());
        tier.setActive(dto.getActive());
        tier.setEligibilityCriteria(dto.getEligibilityCriteria());

        repository.save(tier);
        return mapToDTO(tier);
    }

}
