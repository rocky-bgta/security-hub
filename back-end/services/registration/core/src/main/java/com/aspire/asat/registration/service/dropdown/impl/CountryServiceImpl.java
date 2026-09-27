package com.aspire.asat.registration.service.dropdown.impl;
import com.aspire.asat.registration.data.dropdown.CountryRequestDto;
import com.aspire.asat.registration.data.dropdown.CountryRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.Country;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.service.dropdown.CountryService;
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
public class CountryServiceImpl implements CountryService {
    
    private final CountryRepository countryRepository;

    @Override
    public CountryRespDto createCountry(CountryRequestDto request) {
        log.info("Creating country with code: {}", request.getCode());

        if (countryRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateDataFoundException("Country with code " + request.getCode() + " already exists");
        }
        if (countryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateDataFoundException("Country with name " + request.getName() + " already exists");
        }
        if (countryRepository.existsByPhoneCodeIgnoreCase(request.getPhoneCode())) {
            throw new DuplicateDataFoundException("Country with phone code " + request.getPhoneCode() + " already exists");
        }

        Country country = Country.builder()
                .id(UUID.randomUUID().toString())
                .code(request.getCode())
                .phoneCode(request.getPhoneCode())
                .name(request.getName())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Country savedCountry = countryRepository.save(country);
        log.info("Successfully created country with id: {}", savedCountry.getId());

        return mapToDto(savedCountry);
    }

    @Override
    public List<CountryRespDto> getAllCountries() {
        log.info("Fetching all countries");
        return countryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CountryRespDto> getActiveCountries() {
        log.info("Fetching active countries");
        return countryRepository.findAllActive().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public CountryRespDto getCountryById(String id) {
        log.info("Fetching country by id: {}", id);
        return countryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + id));
    }
    
    @Override
    public CountryRespDto getCountryByCode(String code) {
        log.info("Fetching country by code: {}", code);
        return countryRepository.findByCode(code)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with code: " + code));
    }

    @Override
    public CountryRespDto getCountryByPhoneCode(String phoneCode) {
        log.info("Fetching country by phone code: {}", phoneCode);
        return countryRepository.findByPhoneCode(phoneCode)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with phone code: " + phoneCode));
    }
    
    @Override
    public CountryRespDto updateCountry(String id, CountryRequestDto request) {
        log.info("Updating country with id: {}", id);
        
        Country existingCountry = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + id));
        
        // Check if code is being changed and if new code already exists
        if (!existingCountry.getCode().equalsIgnoreCase(request.getCode()) &&
            countryRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateDataFoundException("Country with code " + request.getCode() + " already exists");
        }

        if (!existingCountry.getName().equalsIgnoreCase(request.getName()) &&
                countryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateDataFoundException("Country with Name " + request.getName() + " already exists");
        }

        String existingPhoneCode = existingCountry.getPhoneCode();
        if ((existingPhoneCode == null || !existingPhoneCode.equalsIgnoreCase(request.getPhoneCode())) &&
                countryRepository.existsByPhoneCodeIgnoreCase(request.getPhoneCode())) {
            throw new DuplicateDataFoundException("Country with phone code " + request.getPhoneCode() + " already exists");
        }
        
        existingCountry.setCode(request.getCode());
        existingCountry.setPhoneCode(request.getPhoneCode());
        existingCountry.setName(request.getName());
        existingCountry.setDisplayOrder(request.getDisplayOrder());
        existingCountry.setActive(request.getActive());
        existingCountry.setUpdatedAt(Instant.now());
        
        Country updatedCountry = countryRepository.save(existingCountry);
        log.info("Successfully updated country with id: {}", updatedCountry.getId());
        
        return mapToDto(updatedCountry);
    }
    
    @Override
    public void deleteCountry(String id) {
        log.info("Deleting country with id: {}", id);
        
        if (!countryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Country not found with id: " + id);
        }
        
        countryRepository.deleteById(id);
        log.info("Successfully deleted country with id: {}", id);
    }
    
    @Override
    public boolean existsByCode(String code) {
        return countryRepository.existsByCode(code);
    }
    
    private CountryRespDto mapToDto(Country country) {
        return CountryRespDto.builder()
                .id(country.getId())
                .code(country.getCode())
                .phoneCode(country.getPhoneCode())
                .name(country.getName())
                .displayOrder(country.getDisplayOrder())
                .active(country.getActive())
                .createdAt(country.getCreatedAt())
                .updatedAt(country.getUpdatedAt())
                .build();
    }
}
