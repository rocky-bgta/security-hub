package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.SuspendReasonRequestDto;
import com.aspire.asat.registration.data.dropdown.SuspendReasonRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.SuspendReason;
import com.aspire.asat.registration.repository.dropdown.SuspendReasonRepository;
import com.aspire.asat.registration.service.dropdown.SuspendReasonService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
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
public class SuspendReasonServiceImpl implements SuspendReasonService {
    
    private final SuspendReasonRepository suspendReasonRepository;
    private final UserCurrentContextService userCurrentContextService;
    
    @Override
    public SuspendReasonRespDto createSuspendReason(SuspendReasonRequestDto request) {
        log.info("Creating suspend reason with name: {}", request.getName());
        
        if (suspendReasonRepository.existsByName(request.getName())) {
            throw new DuplicateDataFoundException("Suspend reason with name " + request.getName() + " already exists");
        }
        
        String currentUserId = getCurrentUserId();
        Instant now = Instant.now();
        
        SuspendReason suspendReason = SuspendReason.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .createdAt(now)
                .createdBy(currentUserId)
                .updatedAt(now)
                .updatedBy(currentUserId)
                .build();
        
        SuspendReason savedSuspendReason = suspendReasonRepository.save(suspendReason);
        log.info("Successfully created suspend reason with id: {}", savedSuspendReason.getId());
        
        return mapToDto(savedSuspendReason);
    }
    
    @Override
    public List<SuspendReasonRespDto> getAllSuspendReasons() {
        log.info("Fetching all suspend reasons");
        return suspendReasonRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SuspendReasonRespDto> getActiveSuspendReasons() {
        log.info("Fetching active suspend reasons");
        return suspendReasonRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public SuspendReasonRespDto getSuspendReasonById(String id) {
        log.info("Fetching suspend reason by id: {}", id);
        return suspendReasonRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Suspend reason not found with id: " + id));
    }
    
    @Override
    public SuspendReasonRespDto updateSuspendReason(String id, SuspendReasonRequestDto request) {
        log.info("Updating suspend reason with id: {}", id);
        
        SuspendReason existingSuspendReason = suspendReasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suspend reason not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!existingSuspendReason.getName().equals(request.getName()) && 
            suspendReasonRepository.existsByName(request.getName())) {
            throw new DuplicateDataFoundException("Suspend reason with name " + request.getName() + " already exists");
        }
        
        String currentUserId = getCurrentUserId();
        
        existingSuspendReason.setName(request.getName());
        existingSuspendReason.setDescription(request.getDescription());
        if (request.getActive() != null) {
            existingSuspendReason.setActive(request.getActive());
        }
        existingSuspendReason.setUpdatedAt(Instant.now());
        existingSuspendReason.setUpdatedBy(currentUserId);
        
        SuspendReason updatedSuspendReason = suspendReasonRepository.save(existingSuspendReason);
        log.info("Successfully updated suspend reason with id: {}", updatedSuspendReason.getId());
        
        return mapToDto(updatedSuspendReason);
    }
    
    @Override
    public void deleteSuspendReason(String id) {
        log.info("Deleting suspend reason with id: {}", id);
        
        if (!suspendReasonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Suspend reason not found with id: " + id);
        }
        
        suspendReasonRepository.deleteById(id);
        log.info("Successfully deleted suspend reason with id: {}", id);
    }
    
    @Override
    public SuspendReasonRespDto updateSuspendReasonStatus(String id, Boolean active) {
        log.info("Updating suspend reason status with id: {} to active: {}", id, active);
        
        SuspendReason existingSuspendReason = suspendReasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suspend reason not found with id: " + id));
        
        String currentUserId = getCurrentUserId();
        
        existingSuspendReason.setActive(active);
        existingSuspendReason.setUpdatedAt(Instant.now());
        existingSuspendReason.setUpdatedBy(currentUserId);
        
        SuspendReason updatedSuspendReason = suspendReasonRepository.save(existingSuspendReason);
        log.info("Successfully updated suspend reason status with id: {} to active: {}", 
                updatedSuspendReason.getId(), active);
        
        return mapToDto(updatedSuspendReason);
    }
    
    private SuspendReasonRespDto mapToDto(SuspendReason suspendReason) {
        return SuspendReasonRespDto.builder()
                .id(suspendReason.getId())
                .name(suspendReason.getName())
                .description(suspendReason.getDescription())
                .active(suspendReason.getActive())
                .createdAt(suspendReason.getCreatedAt())
                .createdBy(suspendReason.getCreatedBy())
                .updatedAt(suspendReason.getUpdatedAt())
                .updatedBy(suspendReason.getUpdatedBy())
                .build();
    }
    
    /**
     * Get current user ID from UserCurrentContextService.
     * Returns "SYSTEM" if user context is not available.
     *
     * @return Current user ID or "SYSTEM"
     */
    private String getCurrentUserId() {
        try {
            return userCurrentContextService.getCurrentUserContext().getUserId();
        } catch (Exception e) {
            log.warn("Could not get current user context, using SYSTEM as default. Error: {}", e.getMessage());
            return "SYSTEM";
        }
    }
}

