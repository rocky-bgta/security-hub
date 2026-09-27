package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.CallToActionCreateRequest;
import com.aspire.asat.phishing.dto.request.CallToActionUpdateRequest;
import com.aspire.asat.phishing.dto.response.CallToActionDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.CallToAction;
import com.aspire.asat.phishing.repository.CallToActionRepository;
import com.aspire.asat.phishing.service.CallToActionService;
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
 * Service implementation for configurable call-to-action entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CallToActionServiceImpl implements CallToActionService {

    private final CallToActionRepository callToActionRepository;

    @Override
    @Transactional
    public CallToActionDto createCallToAction(CallToActionCreateRequest request) {
        String name = request.getName().trim();
        if (callToActionRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Call to action name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        CallToAction entity = CallToAction.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        CallToAction saved = callToActionRepository.save(entity);
        log.info("Created CallToAction id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public CallToActionDto updateCallToAction(String id, CallToActionUpdateRequest request) {
        CallToAction entity = callToActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call to action not found"));
        String name = request.getName().trim();
        if (callToActionRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Call to action name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        CallToAction saved = callToActionRepository.save(entity);
        log.info("Updated CallToAction id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCallToAction(String id) {
        if (!callToActionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Call to action not found");
        }
        callToActionRepository.deleteById(id);
        log.info("Deleted CallToAction id={}", id);
    }

    @Override
    public CallToActionDto getCallToActionById(String id) {
        return callToActionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Call to action not found"));
    }

    @Override
    public List<CallToActionDto> getCallToActions(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<CallToAction> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? callToActionRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : callToActionRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? callToActionRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : callToActionRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countCallToActions(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? callToActionRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : callToActionRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? callToActionRepository.countWhereEffectiveActive()
                : callToActionRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<CallToAction> defaults = callToActionRepository.findAllByIsDefaultTrue();
        List<CallToAction> toSave = new ArrayList<>();
        for (CallToAction c : defaults) {
            if (keepId == null || !keepId.equals(c.getId())) {
                c.setIsDefault(false);
                toSave.add(c);
            }
        }
        if (!toSave.isEmpty()) {
            callToActionRepository.saveAll(toSave);
        }
    }

    private CallToActionDto toDto(CallToAction entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return CallToActionDto.builder()
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
