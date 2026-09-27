package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.OrganizationSizeRequestDto;
import com.aspire.asat.registration.data.dropdown.OrganizationSizeRespDto;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.OrganizationSize;
import com.aspire.asat.registration.repository.dropdown.OrganizationSizeRepository;
import com.aspire.asat.registration.service.dropdown.OrganizationSizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class OrganizationSizeServiceImpl implements OrganizationSizeService {

    private static final Pattern RANGE_START_PATTERN = Pattern.compile("(\\d+)");

    private final OrganizationSizeRepository organizationSizeRepository;

    @Override
    public OrganizationSizeRespDto createOrganizationSize(OrganizationSizeRequestDto request) {
        log.info("Creating organization size: {} with range: {}", request.getName(), request.getRange());

        // --- Unique name check ---
        if (organizationSizeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Organization size name already exists: " + request.getName());
        }

        // --- Unique range check ---
        if (organizationSizeRepository.existsByRangeIgnoreCase(request.getRange())) {
            throw new IllegalArgumentException("Organization size range already exists: " + request.getRange());
        }

        OrganizationSize organizationSize = OrganizationSize.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .range(request.getRange())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        OrganizationSize savedOrganizationSize = organizationSizeRepository.save(organizationSize);
        log.info("Successfully created organization size with id: {}", savedOrganizationSize.getId());

        return mapToDto(savedOrganizationSize);
    }


    @Override
    public OrganizationSizeRespDto getOrganizationSizeById(String id) {
        log.info("Fetching organization size by id: {}", id);
        return organizationSizeRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Organization size not found with id: " + id));
    }

    @Override
    public OrganizationSizeRespDto updateOrganizationSize(String id, OrganizationSizeRequestDto request) {
        log.info("Updating organization size with id: {}", id);

        OrganizationSize existingOrganizationSize = organizationSizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization size not found with id: " + id));

        if (organizationSizeRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Organization size name already exists: " + request.getName());
        }
        if (organizationSizeRepository.existsByRangeIgnoreCaseAndIdNot(request.getRange(), id)) {
            throw new IllegalArgumentException("Organization size range already exists: " + request.getRange());
        }

        existingOrganizationSize.setName(request.getName());
        existingOrganizationSize.setRange(request.getRange());
        existingOrganizationSize.setUpdatedAt(Instant.now());

        OrganizationSize updatedOrganizationSize = organizationSizeRepository.save(existingOrganizationSize);
        log.info("Successfully updated organization size with id: {}", updatedOrganizationSize.getId());

        return mapToDto(updatedOrganizationSize);
    }


    @Override
    public List<OrganizationSizeRespDto> getActiveOrganizationSizes() {
        log.info("Fetching active organization sizes");
        return organizationSizeRepository.findAll().stream()
                .sorted(Comparator.comparingInt(size -> rangeStart(size.getRange())))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteOrganizationSize(String id) {
        log.info("Deleting organization size with id: {}", id);
        
        if (!organizationSizeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Organization size not found with id: " + id);
        }
        
        organizationSizeRepository.deleteById(id);
        log.info("Successfully deleted organization size with id: {}", id);
    }

    private static int rangeStart(String range) {
        if (range == null || range.isBlank()) {
            return Integer.MAX_VALUE;
        }
        Matcher matcher = RANGE_START_PATTERN.matcher(range);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return Integer.MAX_VALUE;
    }
    
    private OrganizationSizeRespDto mapToDto(OrganizationSize organizationSize) {
        return OrganizationSizeRespDto.builder()
                .id(organizationSize.getId())
                .name(organizationSize.getName())
                .range(organizationSize.getRange())
                .createdAt(organizationSize.getCreatedAt())
                .updatedAt(organizationSize.getUpdatedAt())
                .build();
    }
}