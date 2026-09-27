package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.IndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.IndustryRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.Industry;
import com.aspire.asat.registration.repository.dropdown.IndustryRepository;
import com.aspire.asat.registration.repository.metadata.OrganizationTypeRepository;
import com.aspire.asat.registration.service.dropdown.IndustryService;
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
public class IndustryServiceImpl implements IndustryService {
    
    private final IndustryRepository industryRepository;
    private final OrganizationTypeRepository organizationTypeRepository;
    
    @Override
    public IndustryRespDto createIndustry(IndustryRequestDto request) {
        log.info("Creating industry with code: {} for organization type: {}",
                request.getCode(), request.getOrganizationTypeId());

        ensureOrganizationTypeExists(request.getOrganizationTypeId());
        
        if (industryRepository.existsByCode(request.getCode())) {
            throw new DuplicateDataFoundException("Industry with code " + request.getCode() + " already exists");
        }
        
        Industry industry = Industry.builder()
                .id(UUID.randomUUID().toString())
                .organizationTypeId(request.getOrganizationTypeId())
                .code(request.getCode())
                .name(request.getName())
                .active(request.getActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        Industry savedIndustry = industryRepository.save(industry);
        log.info("Successfully created industry with id: {}", savedIndustry.getId());
        
        return mapToDto(savedIndustry);
    }
    
    @Override
    public IndustryRespDto getIndustryById(String id) {
        log.info("Fetching industry by id: {}", id);
        return industryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Industry not found with id: " + id));
    }
    
    @Override
    public IndustryRespDto updateIndustry(String id, IndustryRequestDto request) {
        log.info("Updating industry with id: {}", id);
        
        Industry existingIndustry = industryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Industry not found with id: " + id));

        ensureOrganizationTypeExists(request.getOrganizationTypeId());
        
        // Check if code is being changed and if new code already exists
        if (!existingIndustry.getCode().equals(request.getCode()) && 
            industryRepository.existsByCode(request.getCode())) {
            throw new DuplicateDataFoundException("Industry with code " + request.getCode() + " already exists");
        }

        existingIndustry.setOrganizationTypeId(request.getOrganizationTypeId());
        existingIndustry.setCode(request.getCode());
        existingIndustry.setName(request.getName());
        existingIndustry.setActive(request.getActive());
        existingIndustry.setUpdatedAt(Instant.now());
        
        Industry updatedIndustry = industryRepository.save(existingIndustry);
        log.info("Successfully updated industry with id: {}", updatedIndustry.getId());
        
        return mapToDto(updatedIndustry);
    }
    
    @Override
    public List<IndustryRespDto> getActiveIndustries(String organizationTypeId) {
        String organizationTypeFilter = blankToNull(organizationTypeId);
        log.info("Fetching active industries with organizationTypeId: {}", organizationTypeFilter);
        List<Industry> industries = organizationTypeFilter == null
                ? industryRepository.findByActiveTrue()
                : industryRepository.findByActiveTrueAndOrganizationTypeId(organizationTypeFilter);
        return industries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteIndustry(String id) {
        log.info("Deleting industry with id: {}", id);
        
        if (!industryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Industry not found with id: " + id);
        }
        
        industryRepository.deleteById(id);
        log.info("Successfully deleted industry with id: {}", id);
    }

    private void ensureOrganizationTypeExists(String organizationTypeId) {
        if (!organizationTypeRepository.existsById(organizationTypeId)) {
            throw new ResourceNotFoundException("Organization type not found with id: " + organizationTypeId);
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
    
    private IndustryRespDto mapToDto(Industry industry) {
        return IndustryRespDto.builder()
                .id(industry.getId())
                .organizationTypeId(industry.getOrganizationTypeId())
                .code(industry.getCode())
                .name(industry.getName())
                .active(industry.getActive())
                .createdAt(industry.getCreatedAt())
                .updatedAt(industry.getUpdatedAt())
                .build();
    }
}
