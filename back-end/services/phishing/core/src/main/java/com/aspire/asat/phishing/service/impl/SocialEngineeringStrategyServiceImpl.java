package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.SocialEngineeringStrategyCreateRequest;
import com.aspire.asat.phishing.dto.request.SocialEngineeringStrategyUpdateRequest;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.SocialEngineeringStrategy;
import com.aspire.asat.phishing.repository.SocialEngineeringStrategyRepository;
import com.aspire.asat.phishing.service.SocialEngineeringStrategyService;
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
 * Service implementation for configurable social engineering strategy entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialEngineeringStrategyServiceImpl implements SocialEngineeringStrategyService {

    private final SocialEngineeringStrategyRepository socialEngineeringStrategyRepository;

    @Override
    @Transactional
    public SocialEngineeringStrategyDto createSocialEngineeringStrategy(SocialEngineeringStrategyCreateRequest request) {
        String name = request.getName().trim();
        if (socialEngineeringStrategyRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Social engineering strategy name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        SocialEngineeringStrategy entity = SocialEngineeringStrategy.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        SocialEngineeringStrategy saved = socialEngineeringStrategyRepository.save(entity);
        log.info("Created SocialEngineeringStrategy id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public SocialEngineeringStrategyDto updateSocialEngineeringStrategy(
            String id, SocialEngineeringStrategyUpdateRequest request) {
        SocialEngineeringStrategy entity = socialEngineeringStrategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social engineering strategy not found"));
        String name = request.getName().trim();
        if (socialEngineeringStrategyRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Social engineering strategy name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        SocialEngineeringStrategy saved = socialEngineeringStrategyRepository.save(entity);
        log.info("Updated SocialEngineeringStrategy id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteSocialEngineeringStrategy(String id) {
        if (!socialEngineeringStrategyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Social engineering strategy not found");
        }
        socialEngineeringStrategyRepository.deleteById(id);
        log.info("Deleted SocialEngineeringStrategy id={}", id);
    }

    @Override
    public SocialEngineeringStrategyDto getSocialEngineeringStrategyById(String id) {
        return socialEngineeringStrategyRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Social engineering strategy not found"));
    }

    @Override
    public List<SocialEngineeringStrategyDto> getSocialEngineeringStrategies(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<SocialEngineeringStrategy> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? socialEngineeringStrategyRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : socialEngineeringStrategyRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? socialEngineeringStrategyRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : socialEngineeringStrategyRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countSocialEngineeringStrategies(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? socialEngineeringStrategyRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : socialEngineeringStrategyRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? socialEngineeringStrategyRepository.countWhereEffectiveActive()
                : socialEngineeringStrategyRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<SocialEngineeringStrategy> defaults = socialEngineeringStrategyRepository.findAllByIsDefaultTrue();
        List<SocialEngineeringStrategy> toSave = new ArrayList<>();
        for (SocialEngineeringStrategy s : defaults) {
            if (keepId == null || !keepId.equals(s.getId())) {
                s.setIsDefault(false);
                toSave.add(s);
            }
        }
        if (!toSave.isEmpty()) {
            socialEngineeringStrategyRepository.saveAll(toSave);
        }
    }

    private SocialEngineeringStrategyDto toDto(SocialEngineeringStrategy entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return SocialEngineeringStrategyDto.builder()
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
