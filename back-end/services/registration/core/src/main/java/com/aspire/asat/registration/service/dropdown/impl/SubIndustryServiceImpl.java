package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.SubIndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.SubIndustryRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.SubIndustry;
import com.aspire.asat.registration.repository.dropdown.IndustryRepository;
import com.aspire.asat.registration.repository.dropdown.SubIndustryRepository;
import com.aspire.asat.registration.repository.metadata.OrganizationTypeRepository;
import com.aspire.asat.registration.service.dropdown.SubIndustryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class SubIndustryServiceImpl implements SubIndustryService {

    private final SubIndustryRepository subIndustryRepository;
    private final IndustryRepository industryRepository;
    private final OrganizationTypeRepository organizationTypeRepository;

    @Override
    public SubIndustryRespDto createSubIndustry(SubIndustryRequestDto request) {
        log.info("Creating sub-industry with code: {} for industry: {} and organization type: {}",
                request.getCode(), request.getIndustryId(), request.getOrganizationTypeId());

        ensureOrganizationTypeExists(request.getOrganizationTypeId());
        ensureIndustryExists(request.getIndustryId());

        if (subIndustryRepository.existsByIndustryIdAndCodeIgnoreCase(request.getIndustryId(), request.getCode())) {
            throw new DuplicateDataFoundException("Sub-industry with code " + request.getCode()
                    + " already exists for industry: " + request.getIndustryId());
        }

        SubIndustry subIndustry = SubIndustry.builder()
                .id(UUID.randomUUID().toString())
                .organizationTypeId(request.getOrganizationTypeId())
                .industryId(request.getIndustryId())
                .code(request.getCode())
                .name(request.getName())
                .active(request.getActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        SubIndustry savedSubIndustry = subIndustryRepository.save(subIndustry);
        log.info("Successfully created sub-industry with id: {}", savedSubIndustry.getId());

        return mapToDto(savedSubIndustry);
    }

    @Override
    public SubIndustryRespDto getSubIndustryById(String id) {
        log.info("Fetching sub-industry by id: {}", id);
        return subIndustryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-industry not found with id: " + id));
    }

    @Override
    public SubIndustryRespDto updateSubIndustry(String id, SubIndustryRequestDto request) {
        log.info("Updating sub-industry with id: {}", id);

        SubIndustry existingSubIndustry = subIndustryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-industry not found with id: " + id));

        ensureOrganizationTypeExists(request.getOrganizationTypeId());
        ensureIndustryExists(request.getIndustryId());

        if (subIndustryRepository.existsByIndustryIdAndCodeIgnoreCaseAndIdNot(
                request.getIndustryId(), request.getCode(), id)) {
            throw new DuplicateDataFoundException("Sub-industry with code " + request.getCode()
                    + " already exists for industry: " + request.getIndustryId());
        }

        existingSubIndustry.setOrganizationTypeId(request.getOrganizationTypeId());
        existingSubIndustry.setIndustryId(request.getIndustryId());
        existingSubIndustry.setCode(request.getCode());
        existingSubIndustry.setName(request.getName());
        existingSubIndustry.setActive(request.getActive());
        existingSubIndustry.setUpdatedAt(Instant.now());

        SubIndustry updatedSubIndustry = subIndustryRepository.save(existingSubIndustry);
        log.info("Successfully updated sub-industry with id: {}", updatedSubIndustry.getId());

        return mapToDto(updatedSubIndustry);
    }

    @Override
    public List<SubIndustryRespDto> getActiveSubIndustries(String organizationTypeId, String industryId) {
        String organizationTypeFilter = blankToNull(organizationTypeId);
        String industryFilter = blankToNull(industryId);
        log.info("Fetching active sub-industries with organizationTypeId: {}, industryId: {}",
                organizationTypeFilter, industryFilter);

        List<SubIndustry> subIndustries;
        if (organizationTypeFilter != null && industryFilter != null) {
            subIndustries = subIndustryRepository.findByActiveTrueAndOrganizationTypeIdAndIndustryId(
                    organizationTypeFilter, industryFilter);
        } else if (organizationTypeFilter != null) {
            subIndustries = subIndustryRepository.findByActiveTrueAndOrganizationTypeId(organizationTypeFilter);
        } else if (industryFilter != null) {
            subIndustries = subIndustryRepository.findByActiveTrueAndIndustryId(industryFilter);
        } else {
            subIndustries = subIndustryRepository.findByActiveTrue();
        }

        return subIndustries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubIndustryRespDto> getSubIndustries(String search, Boolean active, String industryId) {
        String searchTerm = search == null ? "" : search.trim();
        String industryFilter = blankToNull(industryId);
        log.info("Fetching sub-industries with search: {}, active: {}, industryId: {}",
                searchTerm, active, industryFilter);
        return subIndustryRepository.findWithFilters(searchTerm, active, industryFilter).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSubIndustry(String id) {
        log.info("Deleting sub-industry with id: {}", id);

        if (!subIndustryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sub-industry not found with id: " + id);
        }

        subIndustryRepository.deleteById(id);
        log.info("Successfully deleted sub-industry with id: {}", id);
    }

    private void ensureOrganizationTypeExists(String organizationTypeId) {
        if (!organizationTypeRepository.existsById(organizationTypeId)) {
            throw new ResourceNotFoundException("Organization type not found with id: " + organizationTypeId);
        }
    }

    private void ensureIndustryExists(String industryId) {
        if (!industryRepository.existsById(industryId)) {
            throw new ResourceNotFoundException("Industry not found with id: " + industryId);
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private SubIndustryRespDto mapToDto(SubIndustry subIndustry) {
        return SubIndustryRespDto.builder()
                .id(subIndustry.getId())
                .organizationTypeId(subIndustry.getOrganizationTypeId())
                .industryId(subIndustry.getIndustryId())
                .code(subIndustry.getCode())
                .name(subIndustry.getName())
                .active(subIndustry.getActive())
                .createdAt(subIndustry.getCreatedAt())
                .updatedAt(subIndustry.getUpdatedAt())
                .build();
    }
}
