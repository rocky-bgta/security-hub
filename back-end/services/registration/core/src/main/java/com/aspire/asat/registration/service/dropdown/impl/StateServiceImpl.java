package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.StateRequestDto;
import com.aspire.asat.registration.data.dropdown.StateRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.State;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.repository.dropdown.StateRepository;
import com.aspire.asat.registration.service.dropdown.StateService;
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
public class StateServiceImpl implements StateService {
    
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    
    @Override
    public StateRespDto createState(StateRequestDto request) {
        log.info("Creating state with code: {} for country: {}", request.getCode(), request.getCountryId());
        
        // Validate that country exists
        if (!countryRepository.existsById(request.getCountryId())) {
            throw new ResourceNotFoundException("Country not found with id: " + request.getCountryId());
        }
        
        if (stateRepository.existsByCountryIdAndCodeIgnoreCase(request.getCountryId(), request.getCode())) {
            throw new DuplicateDataFoundException("State with code " + request.getCode() + " already exists for country: " + request.getCountryId());
        }

        if (stateRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateDataFoundException("State with name " + request.getName() + " already exists for country: " + request.getCountryId());
        }
        
        State state = State.builder()
                .id(UUID.randomUUID().toString())
                .countryId(request.getCountryId())
                .code(request.getCode())
                .name(request.getName())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        State savedState = stateRepository.save(state);
        log.info("Successfully created state with id: {}", savedState.getId());
        
        return mapToDto(savedState);
    }
    
    @Override
    public List<StateRespDto> getAllStates() {
        log.info("Fetching all states");
        return stateRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StateRespDto> getActiveStates() {
        log.info("Fetching active states");
        return stateRepository.findAllActive().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StateRespDto> getStatesByCountryId(String countryId) {
        log.info("Fetching states for country: {}", countryId);
        return stateRepository.findByCountryIdAndActiveTrue(countryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StateRespDto> getActiveStatesByCountryId(String countryId) {
        log.info("Fetching active states for country: {}", countryId);
        return stateRepository.findByCountryIdAndActiveTrueOrderByDisplayOrder(countryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public StateRespDto getStateById(String id) {
        log.info("Fetching state by id: {}", id);
        return stateRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("State not found with id: " + id));
    }
    
    @Override
    public StateRespDto getStateByCode(String code) {
        log.info("Fetching state by code: {}", code);
        return stateRepository.findByCode(code)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("State not found with code: " + code));
    }
    
    @Override
    public StateRespDto updateState(String id, StateRequestDto request) {
        log.info("Updating state with id: {}", id);
        
        State existingState = stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("State not found with id: " + id));
        
        // Validate that country exists
        if (!countryRepository.existsById(request.getCountryId())) {
            throw new ResourceNotFoundException("Country not found with id: " + request.getCountryId());
        }
        
        // Check if code is being changed and if new code already exists for the country
        if (!existingState.getCode().equalsIgnoreCase(request.getCode()) &&
            stateRepository.existsByCountryIdAndCodeIgnoreCase(request.getCountryId(), request.getCode())) {
            throw new DuplicateDataFoundException("State with code " + request.getCode() + " already exists for country: " + request.getCountryId());
        }

        if (stateRepository.existsByCountryIdAndNameIgnoreCaseAndIdNot(request.getCountryId(), request.getName(), id)) {
            throw new DuplicateDataFoundException(
                    "State with name " + request.getName() + " already exists for country: " + request.getCountryId());
        }

        existingState.setCountryId(request.getCountryId());
        existingState.setCode(request.getCode());
        existingState.setName(request.getName());
        existingState.setDisplayOrder(request.getDisplayOrder());
        existingState.setActive(request.getActive());
        existingState.setUpdatedAt(Instant.now());
        
        State updatedState = stateRepository.save(existingState);
        log.info("Successfully updated state with id: {}", updatedState.getId());
        
        return mapToDto(updatedState);
    }
    
    @Override
    public void deleteState(String id) {
        log.info("Deleting state with id: {}", id);
        
        if (!stateRepository.existsById(id)) {
            throw new IllegalArgumentException("State not found with id: " + id);
        }
        
        stateRepository.deleteById(id);
        log.info("Successfully deleted state with id: {}", id);
    }
    
    @Override
    public boolean existsByCode(String code) {
        return stateRepository.existsByCode(code);
    }
    
    @Override
    public boolean existsByCountryIdAndCode(String countryId, String code) {
        return stateRepository.existsByCountryIdAndCode(countryId, code);
    }
    
    private StateRespDto mapToDto(State state) {
        return StateRespDto.builder()
                .id(state.getId())
                .countryId(state.getCountryId())
                .code(state.getCode())
                .name(state.getName())
                .displayOrder(state.getDisplayOrder())
                .active(state.getActive())
                .createdAt(state.getCreatedAt())
                .updatedAt(state.getUpdatedAt())
                .build();
    }
}
