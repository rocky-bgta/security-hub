package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.EmotionalTriggerCreateRequest;
import com.aspire.asat.phishing.dto.request.EmotionalTriggerUpdateRequest;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.EmotionalTrigger;
import com.aspire.asat.phishing.repository.EmotionalTriggerRepository;
import com.aspire.asat.phishing.service.EmotionalTriggerService;
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
 * Service implementation for configurable emotional trigger entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmotionalTriggerServiceImpl implements EmotionalTriggerService {

    private final EmotionalTriggerRepository emotionalTriggerRepository;

    @Override
    @Transactional
    public EmotionalTriggerDto createEmotionalTrigger(EmotionalTriggerCreateRequest request) {
        String name = request.getName().trim();
        if (emotionalTriggerRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Emotional trigger name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        EmotionalTrigger entity = EmotionalTrigger.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        EmotionalTrigger saved = emotionalTriggerRepository.save(entity);
        log.info("Created EmotionalTrigger id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public EmotionalTriggerDto updateEmotionalTrigger(String id, EmotionalTriggerUpdateRequest request) {
        EmotionalTrigger entity = emotionalTriggerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emotional trigger not found"));
        String name = request.getName().trim();
        if (emotionalTriggerRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Emotional trigger name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        EmotionalTrigger saved = emotionalTriggerRepository.save(entity);
        log.info("Updated EmotionalTrigger id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteEmotionalTrigger(String id) {
        if (!emotionalTriggerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Emotional trigger not found");
        }
        emotionalTriggerRepository.deleteById(id);
        log.info("Deleted EmotionalTrigger id={}", id);
    }

    @Override
    public EmotionalTriggerDto getEmotionalTriggerById(String id) {
        return emotionalTriggerRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Emotional trigger not found"));
    }

    @Override
    public List<EmotionalTriggerDto> getEmotionalTriggers(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<EmotionalTrigger> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? emotionalTriggerRepository.searchByNameWhereEffectiveActive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : emotionalTriggerRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? emotionalTriggerRepository.searchByNameWhereInactive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : emotionalTriggerRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countEmotionalTriggers(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? emotionalTriggerRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : emotionalTriggerRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? emotionalTriggerRepository.countWhereEffectiveActive()
                : emotionalTriggerRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<EmotionalTrigger> defaults = emotionalTriggerRepository.findAllByIsDefaultTrue();
        List<EmotionalTrigger> toSave = new ArrayList<>();
        for (EmotionalTrigger p : defaults) {
            if (keepId == null || !keepId.equals(p.getId())) {
                p.setIsDefault(false);
                toSave.add(p);
            }
        }
        if (!toSave.isEmpty()) {
            emotionalTriggerRepository.saveAll(toSave);
        }
    }

    private EmotionalTriggerDto toDto(EmotionalTrigger entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return EmotionalTriggerDto.builder()
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
