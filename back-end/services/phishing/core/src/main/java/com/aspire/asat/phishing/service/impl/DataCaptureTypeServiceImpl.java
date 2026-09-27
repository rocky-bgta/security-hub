package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.DataCaptureTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.DataCaptureTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.DataCaptureType;
import com.aspire.asat.phishing.repository.DataCaptureTypeRepository;
import com.aspire.asat.phishing.service.DataCaptureTypeService;
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
 * Service implementation for configurable data capture type entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataCaptureTypeServiceImpl implements DataCaptureTypeService {

    private final DataCaptureTypeRepository dataCaptureTypeRepository;

    @Override
    @Transactional
    public DataCaptureTypeDto createDataCaptureType(DataCaptureTypeCreateRequest request) {
        String name = request.getName().trim();
        if (dataCaptureTypeRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Data capture type name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        DataCaptureType entity = DataCaptureType.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        DataCaptureType saved = dataCaptureTypeRepository.save(entity);
        log.info("Created DataCaptureType id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public DataCaptureTypeDto updateDataCaptureType(String id, DataCaptureTypeUpdateRequest request) {
        DataCaptureType entity = dataCaptureTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data capture type not found"));
        String name = request.getName().trim();
        if (dataCaptureTypeRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Data capture type name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        DataCaptureType saved = dataCaptureTypeRepository.save(entity);
        log.info("Updated DataCaptureType id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteDataCaptureType(String id) {
        if (!dataCaptureTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Data capture type not found");
        }
        dataCaptureTypeRepository.deleteById(id);
        log.info("Deleted DataCaptureType id={}", id);
    }

    @Override
    public DataCaptureTypeDto getDataCaptureTypeById(String id) {
        return dataCaptureTypeRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Data capture type not found"));
    }

    @Override
    public List<DataCaptureTypeDto> getDataCaptureTypes(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<DataCaptureType> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? dataCaptureTypeRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : dataCaptureTypeRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? dataCaptureTypeRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : dataCaptureTypeRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countDataCaptureTypes(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? dataCaptureTypeRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : dataCaptureTypeRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? dataCaptureTypeRepository.countWhereEffectiveActive()
                : dataCaptureTypeRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<DataCaptureType> defaults = dataCaptureTypeRepository.findAllByIsDefaultTrue();
        List<DataCaptureType> toSave = new ArrayList<>();
        for (DataCaptureType t : defaults) {
            if (keepId == null || !keepId.equals(t.getId())) {
                t.setIsDefault(false);
                toSave.add(t);
            }
        }
        if (!toSave.isEmpty()) {
            dataCaptureTypeRepository.saveAll(toSave);
        }
    }

    private DataCaptureTypeDto toDto(DataCaptureType entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return DataCaptureTypeDto.builder()
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
