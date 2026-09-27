package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.BrandCreateRequest;
import com.aspire.asat.phishing.dto.request.BrandUpdateRequest;
import com.aspire.asat.phishing.dto.response.BrandDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.Brand;
import com.aspire.asat.phishing.repository.BrandRepository;
import com.aspire.asat.phishing.service.BrandService;
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
 * Service implementation for configurable brand entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public BrandDto createBrand(BrandCreateRequest request) {
        String name = request.getName().trim();
        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Brand name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        Brand entity = Brand.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        Brand saved = brandRepository.save(entity);
        log.info("Created Brand id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public BrandDto updateBrand(String id, BrandUpdateRequest request) {
        Brand entity = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        String name = request.getName().trim();
        if (brandRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Brand name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        Brand saved = brandRepository.save(entity);
        log.info("Updated Brand id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteBrand(String id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found");
        }
        brandRepository.deleteById(id);
        log.info("Deleted Brand id={}", id);
    }

    @Override
    public BrandDto getBrandById(String id) {
        return brandRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
    }

    @Override
    public List<BrandDto> getBrands(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<Brand> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? brandRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : brandRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? brandRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : brandRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countBrands(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? brandRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : brandRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? brandRepository.countWhereEffectiveActive()
                : brandRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<Brand> defaults = brandRepository.findAllByIsDefaultTrue();
        List<Brand> toSave = new ArrayList<>();
        for (Brand b : defaults) {
            if (keepId == null || !keepId.equals(b.getId())) {
                b.setIsDefault(false);
                toSave.add(b);
            }
        }
        if (!toSave.isEmpty()) {
            brandRepository.saveAll(toSave);
        }
    }

    private BrandDto toDto(Brand entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return BrandDto.builder()
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
