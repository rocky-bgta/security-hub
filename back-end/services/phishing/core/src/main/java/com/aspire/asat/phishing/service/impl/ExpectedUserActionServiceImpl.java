package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.ExpectedUserActionCreateRequest;
import com.aspire.asat.phishing.dto.request.ExpectedUserActionUpdateRequest;
import com.aspire.asat.phishing.dto.response.ExpectedUserActionDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.ExpectedUserAction;
import com.aspire.asat.phishing.repository.ExpectedUserActionRepository;
import com.aspire.asat.phishing.service.ExpectedUserActionService;
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
 * Service implementation for configurable expected user action entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpectedUserActionServiceImpl implements ExpectedUserActionService {

    private final ExpectedUserActionRepository expectedUserActionRepository;

    @Override
    @Transactional
    public ExpectedUserActionDto createExpectedUserAction(ExpectedUserActionCreateRequest request) {
        String name = request.getName().trim();
        if (expectedUserActionRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Expected user action name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        ExpectedUserAction entity = ExpectedUserAction.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        ExpectedUserAction saved = expectedUserActionRepository.save(entity);
        log.info("Created ExpectedUserAction id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public ExpectedUserActionDto updateExpectedUserAction(String id, ExpectedUserActionUpdateRequest request) {
        ExpectedUserAction entity = expectedUserActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expected user action not found"));
        String name = request.getName().trim();
        if (expectedUserActionRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Expected user action name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        ExpectedUserAction saved = expectedUserActionRepository.save(entity);
        log.info("Updated ExpectedUserAction id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteExpectedUserAction(String id) {
        if (!expectedUserActionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expected user action not found");
        }
        expectedUserActionRepository.deleteById(id);
        log.info("Deleted ExpectedUserAction id={}", id);
    }

    @Override
    public ExpectedUserActionDto getExpectedUserActionById(String id) {
        return expectedUserActionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Expected user action not found"));
    }

    @Override
    public List<ExpectedUserActionDto> getExpectedUserActions(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<ExpectedUserAction> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? expectedUserActionRepository.searchByNameWhereEffectiveActive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : expectedUserActionRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? expectedUserActionRepository.searchByNameWhereInactive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : expectedUserActionRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countExpectedUserActions(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? expectedUserActionRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : expectedUserActionRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? expectedUserActionRepository.countWhereEffectiveActive()
                : expectedUserActionRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<ExpectedUserAction> defaults = expectedUserActionRepository.findAllByIsDefaultTrue();
        List<ExpectedUserAction> toSave = new ArrayList<>();
        for (ExpectedUserAction p : defaults) {
            if (keepId == null || !keepId.equals(p.getId())) {
                p.setIsDefault(false);
                toSave.add(p);
            }
        }
        if (!toSave.isEmpty()) {
            expectedUserActionRepository.saveAll(toSave);
        }
    }

    private ExpectedUserActionDto toDto(ExpectedUserAction entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return ExpectedUserActionDto.builder()
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
