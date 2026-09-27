package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.DeceptionLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.DeceptionLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.DeceptionLevelDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.DeceptionLevel;
import com.aspire.asat.phishing.repository.DeceptionLevelRepository;
import com.aspire.asat.phishing.service.DeceptionLevelService;
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
 * Service implementation for configurable deception level entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeceptionLevelServiceImpl implements DeceptionLevelService {

    private final DeceptionLevelRepository deceptionLevelRepository;

    @Override
    @Transactional
    public DeceptionLevelDto createDeceptionLevel(DeceptionLevelCreateRequest request) {
        String name = request.getName().trim();
        if (deceptionLevelRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Deception level name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        DeceptionLevel entity = DeceptionLevel.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        DeceptionLevel saved = deceptionLevelRepository.save(entity);
        log.info("Created DeceptionLevel id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public DeceptionLevelDto updateDeceptionLevel(String id, DeceptionLevelUpdateRequest request) {
        DeceptionLevel entity = deceptionLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deception level not found"));
        String name = request.getName().trim();
        if (deceptionLevelRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Deception level name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        DeceptionLevel saved = deceptionLevelRepository.save(entity);
        log.info("Updated DeceptionLevel id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteDeceptionLevel(String id) {
        if (!deceptionLevelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Deception level not found");
        }
        deceptionLevelRepository.deleteById(id);
        log.info("Deleted DeceptionLevel id={}", id);
    }

    @Override
    public DeceptionLevelDto getDeceptionLevelById(String id) {
        return deceptionLevelRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Deception level not found"));
    }

    @Override
    public List<DeceptionLevelDto> getDeceptionLevels(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<DeceptionLevel> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? deceptionLevelRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : deceptionLevelRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? deceptionLevelRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : deceptionLevelRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countDeceptionLevels(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? deceptionLevelRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : deceptionLevelRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? deceptionLevelRepository.countWhereEffectiveActive()
                : deceptionLevelRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<DeceptionLevel> defaults = deceptionLevelRepository.findAllByIsDefaultTrue();
        List<DeceptionLevel> toSave = new ArrayList<>();
        for (DeceptionLevel d : defaults) {
            if (keepId == null || !keepId.equals(d.getId())) {
                d.setIsDefault(false);
                toSave.add(d);
            }
        }
        if (!toSave.isEmpty()) {
            deceptionLevelRepository.saveAll(toSave);
        }
    }

    private DeceptionLevelDto toDto(DeceptionLevel entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return DeceptionLevelDto.builder()
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
