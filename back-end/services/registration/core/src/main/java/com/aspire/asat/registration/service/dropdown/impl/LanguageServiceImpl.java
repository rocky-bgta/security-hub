package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.LanguageRequestDto;
import com.aspire.asat.registration.data.dropdown.LanguageRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.Language;
import com.aspire.asat.registration.repository.dropdown.LanguageRepository;
import com.aspire.asat.registration.service.dropdown.LanguageService;
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
public class LanguageServiceImpl implements LanguageService {
    
    private final LanguageRepository languageRepository;
    
    @Override
    public LanguageRespDto createLanguage(LanguageRequestDto request) {
        log.info("Creating language with code: {}", request.getCode());
        
        if (languageRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateDataFoundException("Language with code " + request.getCode() + " already exists");
        }
        if (languageRepository.existsByDisplayNameIgnoreCase(request.getDisplayName())) {
            throw new DuplicateDataFoundException("Language with display name " + request.getDisplayName() + " already exists");
        }
        
        Language language = Language.builder()
                .id(UUID.randomUUID().toString())
                .code(request.getCode())
                .displayName(request.getDisplayName())
                .active(request.getActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        Language savedLanguage = languageRepository.save(language);
        log.info("Successfully created language with id: {}", savedLanguage.getId());
        
        return mapToDto(savedLanguage);
    }
    
    @Override
    public LanguageRespDto getLanguageById(String id) {
        log.info("Fetching language by id: {}", id);
        return languageRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with id: " + id));
    }
    
    @Override
    public List<LanguageRespDto> getActiveLanguages() {
        log.info("Fetching active languages");
        return languageRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public LanguageRespDto updateLanguage(String id, LanguageRequestDto request) {
        log.info("Updating language with id: {}", id);
        
        Language existingLanguage = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with id: " + id));
        
        // Check if code is being changed and if new code already exists
        if (!existingLanguage.getCode().equalsIgnoreCase(request.getCode()) &&
            languageRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateDataFoundException("Language with code " + request.getCode() + " already exists");
        }
        // Check if display name is being changed and if new display name already exists
        if (!existingLanguage.getDisplayName().equalsIgnoreCase(request.getDisplayName()) &&
            languageRepository.existsByDisplayNameIgnoreCase(request.getDisplayName())) {
            throw new DuplicateDataFoundException("Language with display name " + request.getDisplayName() + " already exists");
        }
        
        existingLanguage.setCode(request.getCode());
        existingLanguage.setDisplayName(request.getDisplayName());
        existingLanguage.setActive(request.getActive());
        existingLanguage.setUpdatedAt(Instant.now());
        
        Language updatedLanguage = languageRepository.save(existingLanguage);
        log.info("Successfully updated language with id: {}", updatedLanguage.getId());
        
        return mapToDto(updatedLanguage);
    }
    
    @Override
    public void deleteLanguage(String id) {
        log.info("Deleting language with id: {}", id);
        
        if (!languageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Language not found with id: " + id);
        }
        
        languageRepository.deleteById(id);
        log.info("Successfully deleted language with id: {}", id);
    }
    
    private LanguageRespDto mapToDto(Language language) {
        return LanguageRespDto.builder()
                .id(language.getId())
                .code(language.getCode())
                .displayName(language.getDisplayName())
                .active(language.getActive())
                .createdAt(language.getCreatedAt())
                .updatedAt(language.getUpdatedAt())
                .build();
    }
}
