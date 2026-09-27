package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.data.netTerm.CreateNetTermConfigurationRequestDTO;
import com.aspire.asat.registration.data.netTerm.NetTermConfigurationDropdownDTO;
import com.aspire.asat.registration.data.netTerm.NetTermConfigurationResponseDTO;
import com.aspire.asat.registration.data.netTerm.UpdateNetTermConfigurationRequestDTO;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.model.NetTermConfiguration;
import com.aspire.asat.registration.repository.NetTermConfigurationRepository;
import com.aspire.asat.registration.service.NetTermConfigurationService;
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
public class NetTermConfigurationServiceImpl implements NetTermConfigurationService {

    private final NetTermConfigurationRepository repository;
    private final UserCurrentContextService currentContextService;

    @Override
    public NetTermConfigurationResponseDTO createNetTerm(CreateNetTermConfigurationRequestDTO dto) {
        if (repository.existsByNetTermNameIgnoreCase(dto.getNetTermName())) {
            throw new IllegalArgumentException("Net term configuration with this name already exists");
        }

        String netTermId = UUID.randomUUID().toString();
        String currentUserId = getCurrentUserId();

        Instant now = Instant.now();

        NetTermConfiguration entity = NetTermConfiguration.builder()
                .id(netTermId)
                .netTermName(dto.getNetTermName())
                .netTermInDays(dto.getNetTermInDays())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true) // Default to true
                .createdBy(currentUserId)
                .createdAt(now)
                .updatedBy(currentUserId)
                .updatedAt(now)
                .build();

        repository.save(entity);
        log.info("Created net term configuration with ID: {}", netTermId);
        return mapToDTO(entity);
    }

    @Override
    public List<NetTermConfigurationResponseDTO> getAllNetTerms(String search, Boolean isActive, int offset, int limit) {
        int skipSize = offset == 0 ? 0 : (offset * limit);
        return repository.findAll().stream()
                .filter(n -> (search == null || n.getNetTermName().toLowerCase().contains(search.toLowerCase())))
                .filter(n -> (isActive == null || (n.getIsActive() != null && n.getIsActive().equals(isActive))))
                .skip(skipSize)
                .limit(limit)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public long getTotalNetTermCount(String search, Boolean isActive) {
        return repository.findAll().stream()
                .filter(n -> (search == null || n.getNetTermName().toLowerCase().contains(search.toLowerCase())))
                .filter(n -> (isActive == null || (n.getIsActive() != null && n.getIsActive().equals(isActive))))
                .count();
    }

    @Override
    public NetTermConfigurationResponseDTO getNetTermById(String id) {
        NetTermConfiguration netTerm = repository.findById(id)
                .orElseThrow(() -> new RegistrationServiceException("Net term configuration not found with ID: " + id));
        return mapToDTO(netTerm);
    }

    @Override
    public NetTermConfigurationResponseDTO updateNetTerm(String id, UpdateNetTermConfigurationRequestDTO dto) {
        NetTermConfiguration netTerm = repository.findById(id)
                .orElseThrow(() -> new RegistrationServiceException("Net term configuration not found with ID: " + id));

        // Check if name is being changed and if new name already exists
        if (!netTerm.getNetTermName().equalsIgnoreCase(dto.getNetTermName()) &&
                repository.existsByNetTermNameIgnoreCase(dto.getNetTermName())) {
            throw new IllegalArgumentException("Net term configuration with this name already exists");
        }

        String currentUserId = getCurrentUserId();
        Instant now = Instant.now();

        netTerm.setNetTermName(dto.getNetTermName());
        netTerm.setNetTermInDays(dto.getNetTermInDays());
        netTerm.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        netTerm.setUpdatedBy(currentUserId);
        netTerm.setUpdatedAt(now);

        repository.save(netTerm);
        log.info("Updated net term configuration with ID: {}", id);
        return mapToDTO(netTerm);
    }

    @Override
    public void deleteNetTerm(String id) {
        if (!repository.existsById(id)) {
            throw new RegistrationServiceException("Net term configuration not found with ID: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted net term configuration with ID: {}", id);
    }

    @Override
    public List<NetTermConfigurationDropdownDTO> getAllActiveNetTerms() {
        return repository.findAll().stream()
                .filter(n -> n.getIsActive() != null && n.getIsActive())
                .map(this::mapToDropdownDTO)
                .toList();
    }

    private NetTermConfigurationResponseDTO mapToDTO(NetTermConfiguration netTerm) {
        return NetTermConfigurationResponseDTO.builder()
                .id(netTerm.getId())
                .netTermName(netTerm.getNetTermName())
                .netTermInDays(netTerm.getNetTermInDays())
                .isActive(netTerm.getIsActive())
                .createdBy(netTerm.getCreatedBy())
                .createdAt(netTerm.getCreatedAt())
                .updatedBy(netTerm.getUpdatedBy())
                .updatedAt(netTerm.getUpdatedAt())
                .build();
    }

    private NetTermConfigurationDropdownDTO mapToDropdownDTO(NetTermConfiguration netTerm) {
        return NetTermConfigurationDropdownDTO.builder()
                .id(netTerm.getId())
                .netTermName(netTerm.getNetTermName())
                .netTermInDays(netTerm.getNetTermInDays())
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

