package com.aspire.asat.registration.service.metadata;

import com.aspire.asat.registration.data.metadata.request.OrganizationTypeRequestDTO;
import com.aspire.asat.registration.data.metadata.request.OrganizationTypeUpdateRequestDTO;
import com.aspire.asat.registration.data.metadata.response.OrganizationTypeList;
import com.aspire.asat.registration.data.metadata.response.OrganizationTypeResponseDTO;
import com.aspire.asat.registration.exception.OrganizationTypeAlreadyExistsException;
import com.aspire.asat.registration.exception.OrganizationTypeNotFoundException;
import com.aspire.asat.registration.model.metadata.OrganizationType;
import com.aspire.asat.registration.repository.metadata.OrganizationTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationTypeServiceImpl implements OrganizationTypeService {

    private final OrganizationTypeRepository organizationTypeRepository;


    @Override
    public OrganizationTypeResponseDTO createOrganizationType(OrganizationTypeRequestDTO requestDTO) {
        log.info("Creating organization type: {}", requestDTO.getOrganizationType());

        // Check if organization type already exists
        if (organizationTypeRepository.existsByNameIgnoreCaseAndIsActiveTrue(requestDTO.getOrganizationType())) {
            throw new OrganizationTypeAlreadyExistsException("Organization type already exists: " + requestDTO.getOrganizationType());
        }

        Instant now = Instant.now();

        OrganizationType organizationType = OrganizationType.builder()
                .id(UUID.randomUUID().toString())
                .name(requestDTO.getOrganizationType())
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .updatedBy("system")
                .isActive(true)
                .build();

        OrganizationType savedOrganizationType = organizationTypeRepository.save(organizationType);
        log.info("Successfully created organization type with ID: {}", savedOrganizationType.getId());

        return mapToResponseDTO(savedOrganizationType);
    }

    @Override
    public OrganizationTypeResponseDTO getOrganizationTypeById(String id) {
        log.info("Retrieving organization type by ID: {}", id);

        OrganizationType organizationType = organizationTypeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new OrganizationTypeNotFoundException("Organization type not found with ID: " + id));

        return mapToResponseDTO(organizationType);
    }

    @Override
    public List<OrganizationTypeList> getAllOrganizationTypes() {
        log.info("Retrieving all active organization types");

        List<OrganizationType> organizationTypes = organizationTypeRepository.findByIsActiveTrue();

        return organizationTypes.stream()
                .map(this::mapToList)
                .toList();
    }

    private OrganizationTypeList mapToList(OrganizationType organizationType) {
        return OrganizationTypeList.builder()
                .id(organizationType.getId())
                .organizationType(organizationType.getName())
                .build();
    }

    @Override
    public OrganizationTypeResponseDTO updateOrganizationType(OrganizationTypeUpdateRequestDTO requestDTO) {
        log.info("Updating organization type with ID: {}", requestDTO.getId());

        OrganizationType existingOrganizationType = organizationTypeRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new OrganizationTypeNotFoundException("Organization type not found with ID: " + requestDTO.getId()));

        // Check if the new organization type name already exists (excluding current record)
        if (requestDTO.getName() != null &&
                !requestDTO.getName().equalsIgnoreCase(existingOrganizationType.getName()) &&
                organizationTypeRepository.existsByNameIgnoreCaseAndIsActiveTrue(requestDTO.getName())) {
            throw new OrganizationTypeAlreadyExistsException("Organization type already exists: " + requestDTO.getName());
        }

        // Update fields if provided
        if (requestDTO.getName() != null) {
            existingOrganizationType.setName(requestDTO.getName());
        }
        if (requestDTO.getIsActive() != null) {
            existingOrganizationType.setIsActive(requestDTO.getIsActive());
        }

        existingOrganizationType.setUpdatedAt(Instant.now());
        existingOrganizationType.setUpdatedBy("system");

        OrganizationType updatedOrganizationType = organizationTypeRepository.save(existingOrganizationType);
        log.info("Successfully updated organization type with ID: {}", updatedOrganizationType.getId());

        return mapToResponseDTO(updatedOrganizationType);
    }

    @Override
    public void deleteOrganizationType(String id) {
        log.info("Soft deleting organization type with ID: {}", id);

        OrganizationType organizationType = organizationTypeRepository.findById(id)
                .orElseThrow(() -> new OrganizationTypeNotFoundException("Organization type not found with ID: " + id));

        organizationType.setIsActive(false);
        organizationType.setUpdatedAt(Instant.now());
        organizationType.setUpdatedBy("system");

        organizationTypeRepository.save(organizationType);
        log.info("Successfully soft deleted organization type with ID: {}", id);
    }

    private OrganizationTypeResponseDTO mapToResponseDTO(OrganizationType organizationType) {
        return OrganizationTypeResponseDTO.builder()
                .id(organizationType.getId())
                .organizationType(organizationType.getName())
                .createdAt(organizationType.getCreatedAt())
                .updatedAt(organizationType.getUpdatedAt())
                .createdBy(organizationType.getCreatedBy())
                .updatedBy(organizationType.getUpdatedBy())
                .isActive(organizationType.getIsActive())
                .build();
    }
}
