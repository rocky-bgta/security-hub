package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.LandingPageCategoryCreateRequest;
import com.aspire.asat.phishing.dto.request.LandingPageCategoryUpdateRequest;
import com.aspire.asat.phishing.dto.response.LandingPageCategoryDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.mapper.CatalogDtoMapper;
import com.aspire.asat.phishing.model.LandingPageCategory;
import com.aspire.asat.phishing.repository.LandingPageCategoryRepository;
import com.aspire.asat.phishing.service.LandingPageCategoryService;
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
 * Service implementation for configurable landing page category entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LandingPageCategoryServiceImpl implements LandingPageCategoryService {

    private final LandingPageCategoryRepository landingPageCategoryRepository;
    private final CatalogDtoMapper catalogDtoMapper;

    @Override
    @Transactional
    public LandingPageCategoryDto createLandingPageCategory(LandingPageCategoryCreateRequest request) {
        String name = request.getName().trim();
        if (landingPageCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Landing page category name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        LandingPageCategory entity = LandingPageCategory.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        LandingPageCategory saved = landingPageCategoryRepository.save(entity);
        log.info("Created LandingPageCategory id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public LandingPageCategoryDto updateLandingPageCategory(String id, LandingPageCategoryUpdateRequest request) {
        LandingPageCategory entity = landingPageCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page category not found"));
        String name = request.getName().trim();
        if (landingPageCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Landing page category name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        LandingPageCategory saved = landingPageCategoryRepository.save(entity);
        log.info("Updated LandingPageCategory id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteLandingPageCategory(String id) {
        if (!landingPageCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Landing page category not found");
        }
        landingPageCategoryRepository.deleteById(id);
        log.info("Deleted LandingPageCategory id={}", id);
    }

    @Override
    public LandingPageCategoryDto getLandingPageCategoryById(String id) {
        return landingPageCategoryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page category not found"));
    }

    @Override
    public List<LandingPageCategoryDto> getLandingPageCategories(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<LandingPageCategory> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? landingPageCategoryRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : landingPageCategoryRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? landingPageCategoryRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : landingPageCategoryRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countLandingPageCategories(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? landingPageCategoryRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : landingPageCategoryRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? landingPageCategoryRepository.countWhereEffectiveActive()
                : landingPageCategoryRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<LandingPageCategory> defaults = landingPageCategoryRepository.findAllByIsDefaultTrue();
        List<LandingPageCategory> toSave = new ArrayList<>();
        for (LandingPageCategory c : defaults) {
            if (keepId == null || !keepId.equals(c.getId())) {
                c.setIsDefault(false);
                toSave.add(c);
            }
        }
        if (!toSave.isEmpty()) {
            landingPageCategoryRepository.saveAll(toSave);
        }
    }

    private LandingPageCategoryDto toDto(LandingPageCategory entity) {
        return catalogDtoMapper.toCategoryDto(entity);
    }
}
