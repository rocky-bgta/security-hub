package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.ConstraintsDataCreateRequest;
import com.aspire.asat.phishing.dto.request.ConstraintsDataUpdateRequest;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.ConstraintsData;
import com.aspire.asat.phishing.repository.ConstraintsDataRepository;
import com.aspire.asat.phishing.service.ConstraintsDataService;
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
 * Service implementation for configurable constraints data entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConstraintsDataServiceImpl implements ConstraintsDataService {

    private final ConstraintsDataRepository constraintsDataRepository;

    @Override
    @Transactional
    public ConstraintsDataDto createConstraintsData(ConstraintsDataCreateRequest request) {
        String name = request.getName().trim();
        if (constraintsDataRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Constraints data name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        ConstraintsData entity = ConstraintsData.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        ConstraintsData saved = constraintsDataRepository.save(entity);
        log.info("Created ConstraintsData id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public ConstraintsDataDto updateConstraintsData(String id, ConstraintsDataUpdateRequest request) {
        ConstraintsData entity = constraintsDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Constraints data not found"));
        String name = request.getName().trim();
        if (constraintsDataRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Constraints data name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        ConstraintsData saved = constraintsDataRepository.save(entity);
        log.info("Updated ConstraintsData id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteConstraintsData(String id) {
        if (!constraintsDataRepository.existsById(id)) {
            throw new ResourceNotFoundException("Constraints data not found");
        }
        constraintsDataRepository.deleteById(id);
        log.info("Deleted ConstraintsData id={}", id);
    }

    @Override
    public ConstraintsDataDto getConstraintsDataById(String id) {
        return constraintsDataRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Constraints data not found"));
    }

    @Override
    public List<ConstraintsDataDto> getConstraintsDataList(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<ConstraintsData> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? constraintsDataRepository.searchByNameWhereEffectiveActive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : constraintsDataRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? constraintsDataRepository.searchByNameWhereInactive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : constraintsDataRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countConstraintsData(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? constraintsDataRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : constraintsDataRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? constraintsDataRepository.countWhereEffectiveActive()
                : constraintsDataRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<ConstraintsData> defaults = constraintsDataRepository.findAllByIsDefaultTrue();
        List<ConstraintsData> toSave = new ArrayList<>();
        for (ConstraintsData p : defaults) {
            if (keepId == null || !keepId.equals(p.getId())) {
                p.setIsDefault(false);
                toSave.add(p);
            }
        }
        if (!toSave.isEmpty()) {
            constraintsDataRepository.saveAll(toSave);
        }
    }

    private ConstraintsDataDto toDto(ConstraintsData entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return ConstraintsDataDto.builder()
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
