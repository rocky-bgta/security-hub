package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.TimezoneRequestDto;
import com.aspire.asat.registration.data.dropdown.TimezoneRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.State;
import com.aspire.asat.registration.model.dropdown.Timezone;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.repository.dropdown.StateRepository;
import com.aspire.asat.registration.repository.dropdown.TimezoneRepository;
import com.aspire.asat.registration.service.dropdown.TimezoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class TimezoneServiceImpl implements TimezoneService {
    
    private final TimezoneRepository timezoneRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    
    @Override
    public TimezoneRespDto createTimezone(TimezoneRequestDto request) {
        log.info("Creating timezone with timezoneId: {} for state: {}", request.getTimezoneId(), request.getStateId());

        // Validate that country exists
        if (!countryRepository.existsById(request.getCountryId())) {
            throw new ResourceNotFoundException("Country not found with id: " + request.getCountryId());
        }

        // Validate that state exists
        if (!stateRepository.existsById(request.getStateId())) {
            throw new ResourceNotFoundException("State not found with id: " + request.getStateId());
        }

        // Check for duplicate timezone
        if (timezoneRepository.existsByStateIdAndTimezoneIdIgnoreCase(request.getStateId(), request.getTimezoneId())) {
            throw new DuplicateDataFoundException("Timezone with timezoneId " + request.getTimezoneId() + " already exists for state: " + request.getStateId());
        }
        
        Timezone timezone = Timezone.builder()
                .id(UUID.randomUUID().toString())
                .countryId(request.getCountryId())
                .stateId(request.getStateId())
                .timezoneId(request.getTimezoneId())
                .displayName(request.getDisplayName())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        Timezone savedTimezone = timezoneRepository.save(timezone);
        log.info("Successfully created timezone with id: {}", savedTimezone.getId());
        
        return mapToDto(savedTimezone);
    }
    
    @Override
    public List<TimezoneRespDto> getAllTimezones() {
        log.info("Fetching all timezones");
        return timezoneRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TimezoneRespDto> getActiveTimezones() {
        log.info("Fetching active timezones");
        List<Timezone> timezones = timezoneRepository.findAllActive();

        List<String> stateIds = timezones.stream()
                .map(Timezone::getStateId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> stateNameById = stateRepository.findAllById(stateIds).stream()
                .collect(Collectors.toMap(State::getId, State::getName));

        return timezones.stream()
                .map(timezone -> mapToDto(timezone, stateNameById.get(timezone.getStateId())))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TimezoneRespDto> getTimezonesByStateId(String stateId) {
        log.info("Fetching timezones for state: {}", stateId);
        return timezoneRepository.findByStateIdAndActiveTrue(stateId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TimezoneRespDto> getActiveTimezonesByStateId(String stateId) {
        log.info("Fetching active timezones for state: {}", stateId);
        return timezoneRepository.findByStateIdAndActiveTrueOrderByDisplayOrder(stateId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TimezoneRespDto> getTimezonesByCountryId(String countryId) {
        log.info("Fetching timezones for country: {}", countryId);
        return timezoneRepository.findByCountryIdAndActiveTrue(countryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TimezoneRespDto> getActiveTimezonesByCountryId(String countryId) {
        log.info("Fetching active timezones for country: {}", countryId);
        return timezoneRepository.findByCountryIdAndActiveTrue(countryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public TimezoneRespDto getTimezoneById(String id) {
        log.info("Fetching timezone by id: {}", id);
        return timezoneRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Timezone not found with id: " + id));
    }
    
    @Override
    public TimezoneRespDto getTimezoneByTimezoneId(String timezoneId) {
        log.info("Fetching timezone by timezoneId: {}", timezoneId);
        return timezoneRepository.findByTimezoneId(timezoneId)
                .map(this::mapToDto)
                .orElseThrow(() -> new IllegalArgumentException("Timezone not found with timezoneId: " + timezoneId));
    }
    
    @Override
    public TimezoneRespDto updateTimezone(String id, TimezoneRequestDto request) {
        log.info("Updating timezone with id: {}", id);
        
        Timezone existingTimezone = timezoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timezone not found with id: " + id));

        // Validate that country exists
        if (!countryRepository.existsById(request.getCountryId())) {
            throw new ResourceNotFoundException("Country not found with id: " + request.getCountryId());
        }

        // Validate that state exists
        if (!stateRepository.existsById(request.getStateId())) {
            throw new ResourceNotFoundException("State not found with id: " + request.getStateId());
        }
        
        // Check if timezoneId is being changed and if new timezoneId already exists for the state
        if (!existingTimezone.getTimezoneId().equalsIgnoreCase(request.getTimezoneId()) &&
            timezoneRepository.existsByStateIdAndTimezoneIdIgnoreCase(request.getStateId(), request.getTimezoneId())) {
            throw new DuplicateDataFoundException("Timezone with timezoneId " + request.getTimezoneId() + " already exists for state: " + request.getStateId());
        }
        
        existingTimezone.setCountryId(request.getCountryId());
        existingTimezone.setStateId(request.getStateId());
        existingTimezone.setTimezoneId(request.getTimezoneId());
        existingTimezone.setDisplayName(request.getDisplayName());
        existingTimezone.setDisplayOrder(request.getDisplayOrder());
        existingTimezone.setActive(request.getActive());
        existingTimezone.setUpdatedAt(Instant.now());
        
        Timezone updatedTimezone = timezoneRepository.save(existingTimezone);
        log.info("Successfully updated timezone with id: {}", updatedTimezone.getId());
        
        return mapToDto(updatedTimezone);
    }
    
    @Override
    public void deleteTimezone(String id) {
        log.info("Deleting timezone with id: {}", id);
        
        if (!timezoneRepository.existsById(id)) {
            throw new IllegalArgumentException("Timezone not found with id: " + id);
        }
        
        timezoneRepository.deleteById(id);
        log.info("Successfully deleted timezone with id: {}", id);
    }
    
    @Override
    public boolean existsByTimezoneId(String timezoneId) {
        return timezoneRepository.existsByTimezoneId(timezoneId);
    }
    
    @Override
    public boolean existsByStateIdAndTimezoneId(String stateId, String timezoneId) {
        return timezoneRepository.existsByStateIdAndTimezoneId(stateId, timezoneId);
    }
    
    private TimezoneRespDto mapToDto(Timezone timezone) {
        return mapToDto(timezone, null);
    }

    private TimezoneRespDto mapToDto(Timezone timezone, String stateName) {
        return TimezoneRespDto.builder()
                .id(timezone.getId())
                .countryId(timezone.getCountryId())
                .stateId(timezone.getStateId())
                .stateName(stateName)
                .timezoneId(timezone.getTimezoneId())
                .displayName(timezone.getDisplayName())
                .displayOrder(timezone.getDisplayOrder())
                .active(timezone.getActive())
                .createdAt(timezone.getCreatedAt())
                .updatedAt(timezone.getUpdatedAt())
                .build();
    }
}
