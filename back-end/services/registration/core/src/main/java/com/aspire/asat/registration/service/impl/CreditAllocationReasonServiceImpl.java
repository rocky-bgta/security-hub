package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.data.creditAllocation.CreateCreditAllocationReasonRequestDTO;
import com.aspire.asat.registration.data.creditAllocation.CreditAllocationReasonDropdownDTO;
import com.aspire.asat.registration.data.creditAllocation.CreditAllocationReasonResponseDTO;
import com.aspire.asat.registration.data.creditAllocation.UpdateCreditAllocationReasonRequestDTO;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.model.CreditAllocationReason;
import com.aspire.asat.registration.repository.CreditAllocationReasonRepository;
import com.aspire.asat.registration.service.CreditAllocationReasonService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditAllocationReasonServiceImpl implements CreditAllocationReasonService {

    private final CreditAllocationReasonRepository repository;
    private final UserCurrentContextService currentContextService;

    @Override
    public CreditAllocationReasonResponseDTO createReason(CreateCreditAllocationReasonRequestDTO dto) {
        if (repository.existsByReasonNameIgnoreCase(dto.getReasonName())) {
            throw new IllegalArgumentException("Credit allocation reason with this name already exists");
        }

        String reasonId = UUID.randomUUID().toString();
        String currentUserId = getCurrentUserId();

        Instant now = Instant.now();

        CreditAllocationReason entity = CreditAllocationReason.builder()
                .id(reasonId)
                .reasonName(dto.getReasonName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true) // Default to true
                .createdBy(currentUserId)
                .createdAt(now)
                .updatedBy(currentUserId)
                .updatedAt(now)
                .build();

        repository.save(entity);
        log.info("Created credit allocation reason with ID: {}", reasonId);
        return mapToDTO(entity);
    }

    @Override
    public List<CreditAllocationReasonResponseDTO> getAllReasons(String search, Boolean isActive, int offset, int limit) {
        int skipSize = offset == 0 ? 0 : (offset * limit);
        return repository.findAll().stream()
                .filter(r -> (search == null || r.getReasonName().toLowerCase().contains(search.toLowerCase()) ||
                        (r.getDescription() != null && r.getDescription().toLowerCase().contains(search.toLowerCase()))))
                .filter(r -> (isActive == null || (r.getIsActive() != null && r.getIsActive().equals(isActive))))
                .skip(skipSize)
                .limit(limit)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public long getTotalReasonCount(String search, Boolean isActive) {
        return repository.findAll().stream()
                .filter(r -> (search == null || r.getReasonName().toLowerCase().contains(search.toLowerCase()) ||
                        (r.getDescription() != null && r.getDescription().toLowerCase().contains(search.toLowerCase()))))
                .filter(r -> (isActive == null || (r.getIsActive() != null && r.getIsActive().equals(isActive))))
                .count();
    }

    @Override
    public CreditAllocationReasonResponseDTO getReasonById(String id) {
        CreditAllocationReason reason = repository.findById(id)
                .orElseThrow(() -> new RegistrationServiceException("Credit allocation reason not found with ID: " + id));
        return mapToDTO(reason);
    }

    @Override
    public CreditAllocationReasonResponseDTO updateReason(String id, UpdateCreditAllocationReasonRequestDTO dto) {
        CreditAllocationReason reason = repository.findById(id)
                .orElseThrow(() -> new RegistrationServiceException("Credit allocation reason not found with ID: " + id));

        // Check if name is being changed and if new name already exists
        if (!reason.getReasonName().equalsIgnoreCase(dto.getReasonName()) &&
                repository.existsByReasonNameIgnoreCase(dto.getReasonName())) {
            throw new IllegalArgumentException("Credit allocation reason with this name already exists");
        }

        String currentUserId = getCurrentUserId();
        Instant now = Instant.now();

        reason.setReasonName(dto.getReasonName());
        reason.setDescription(dto.getDescription());
        reason.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        reason.setUpdatedBy(currentUserId);
        reason.setUpdatedAt(now);

        repository.save(reason);
        log.info("Updated credit allocation reason with ID: {}", id);
        return mapToDTO(reason);
    }

    @Override
    public void deleteReason(String id) {
        if (!repository.existsById(id)) {
            throw new RegistrationServiceException("Credit allocation reason not found with ID: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted credit allocation reason with ID: {}", id);
    }

    @Override
    public List<CreditAllocationReasonDropdownDTO> getAllActiveReasons() {
        return repository.findAll().stream()
                .filter(r -> r.getIsActive() != null && r.getIsActive())
                .map(this::mapToDropdownDTO)
                .toList();
    }

    private CreditAllocationReasonResponseDTO mapToDTO(CreditAllocationReason reason) {
        return CreditAllocationReasonResponseDTO.builder()
                .id(reason.getId())
                .reasonName(reason.getReasonName())
                .description(reason.getDescription())
                .isActive(reason.getIsActive())
                .createdBy(reason.getCreatedBy())
                .createdAt(reason.getCreatedAt())
                .updatedBy(reason.getUpdatedBy())
                .updatedAt(reason.getUpdatedAt())
                .build();
    }

    private CreditAllocationReasonDropdownDTO mapToDropdownDTO(CreditAllocationReason reason) {
        return CreditAllocationReasonDropdownDTO.builder()
                .id(reason.getId())
                .reasonName(reason.getReasonName())
                .description(reason.getDescription())
                .build();
    }

    private String getCurrentUserId() {
        try {
            CurrentUserContext userContext = currentContextService.getCurrentUserContext();
            return userContext != null && userContext.getUserId() != null ? userContext.getUserId() : "SYSTEM";
        } catch (Exception e) {
            log.warn("Could not get current user context, using SYSTEM as default: {}", e.getMessage());
            return "SYSTEM";
        }
    }
}

