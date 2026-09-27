package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.UrgencyLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.UrgencyLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.UrgencyLevel;
import com.aspire.asat.phishing.repository.UrgencyLevelRepository;
import com.aspire.asat.phishing.service.UrgencyLevelService;
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
 * Service implementation for configurable urgency level entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrgencyLevelServiceImpl implements UrgencyLevelService {

    private final UrgencyLevelRepository urgencyLevelRepository;

    @Override
    @Transactional
    public UrgencyLevelDto createUrgencyLevel(UrgencyLevelCreateRequest request) {
        String name = request.getName().trim();
        if (urgencyLevelRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Urgency level name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        UrgencyLevel entity = UrgencyLevel.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        UrgencyLevel saved = urgencyLevelRepository.save(entity);
        log.info("Created UrgencyLevel id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public UrgencyLevelDto updateUrgencyLevel(String id, UrgencyLevelUpdateRequest request) {
        UrgencyLevel entity = urgencyLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Urgency level not found"));
        String name = request.getName().trim();
        if (urgencyLevelRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Urgency level name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        UrgencyLevel saved = urgencyLevelRepository.save(entity);
        log.info("Updated UrgencyLevel id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUrgencyLevel(String id) {
        if (!urgencyLevelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Urgency level not found");
        }
        urgencyLevelRepository.deleteById(id);
        log.info("Deleted UrgencyLevel id={}", id);
    }

    @Override
    public UrgencyLevelDto getUrgencyLevelById(String id) {
        return urgencyLevelRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Urgency level not found"));
    }

    @Override
    public List<UrgencyLevelDto> getUrgencyLevels(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<UrgencyLevel> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? urgencyLevelRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : urgencyLevelRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? urgencyLevelRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : urgencyLevelRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countUrgencyLevels(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? urgencyLevelRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : urgencyLevelRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? urgencyLevelRepository.countWhereEffectiveActive()
                : urgencyLevelRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<UrgencyLevel> defaults = urgencyLevelRepository.findAllByIsDefaultTrue();
        List<UrgencyLevel> toSave = new ArrayList<>();
        for (UrgencyLevel level : defaults) {
            if (keepId == null || !keepId.equals(level.getId())) {
                level.setIsDefault(false);
                toSave.add(level);
            }
        }
        if (!toSave.isEmpty()) {
            urgencyLevelRepository.saveAll(toSave);
        }
    }

    private UrgencyLevelDto toDto(UrgencyLevel entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return UrgencyLevelDto.builder()
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
