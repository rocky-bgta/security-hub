package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.PersonalizationLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.PersonalizationLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.PersonalizationLevelDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.PersonalizationLevel;
import com.aspire.asat.phishing.repository.PersonalizationLevelRepository;
import com.aspire.asat.phishing.service.PersonalizationLevelService;
import com.aspire.asat.phishing.service.support.CatalogCrudSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for configurable personalization level entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalizationLevelServiceImpl implements PersonalizationLevelService {

    private final PersonalizationLevelRepository personalizationLevelRepository;

    @Override
    @Transactional
    public PersonalizationLevelDto createPersonalizationLevel(PersonalizationLevelCreateRequest request) {
        String name = request.getName().trim();
        if (personalizationLevelRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Personalization level name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        PersonalizationLevel entity = PersonalizationLevel.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        PersonalizationLevel saved = personalizationLevelRepository.save(entity);
        log.info("Created PersonalizationLevel id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public PersonalizationLevelDto updatePersonalizationLevel(String id, PersonalizationLevelUpdateRequest request) {
        PersonalizationLevel entity = personalizationLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personalization level not found"));
        String name = request.getName().trim();
        if (personalizationLevelRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Personalization level name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        PersonalizationLevel saved = personalizationLevelRepository.save(entity);
        log.info("Updated PersonalizationLevel id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deletePersonalizationLevel(String id) {
        if (!personalizationLevelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Personalization level not found");
        }
        personalizationLevelRepository.deleteById(id);
        log.info("Deleted PersonalizationLevel id={}", id);
    }

    @Override
    public PersonalizationLevelDto getPersonalizationLevelById(String id) {
        return personalizationLevelRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Personalization level not found"));
    }

    @Override
    public List<PersonalizationLevelDto> getPersonalizationLevels(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<PersonalizationLevel> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? personalizationLevelRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : personalizationLevelRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? personalizationLevelRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : personalizationLevelRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countPersonalizationLevels(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? personalizationLevelRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : personalizationLevelRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? personalizationLevelRepository.countWhereEffectiveActive()
                : personalizationLevelRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<PersonalizationLevel> defaults = personalizationLevelRepository.findAllByIsDefaultTrue();
        List<PersonalizationLevel> toSave = new ArrayList<>();
        for (PersonalizationLevel p : defaults) {
            if (keepId == null || !keepId.equals(p.getId())) {
                p.setIsDefault(false);
                toSave.add(p);
            }
        }
        if (!toSave.isEmpty()) {
            personalizationLevelRepository.saveAll(toSave);
        }
    }

    private PersonalizationLevelDto toDto(PersonalizationLevel entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return PersonalizationLevelDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .displayOrder(order)
                .isDefault(def)
                .isActive(active)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
