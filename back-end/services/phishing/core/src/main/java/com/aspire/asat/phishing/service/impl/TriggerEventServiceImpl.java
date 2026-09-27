package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.TriggerEventCreateRequest;
import com.aspire.asat.phishing.dto.request.TriggerEventUpdateRequest;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.TriggerEvent;
import com.aspire.asat.phishing.repository.TriggerEventRepository;
import com.aspire.asat.phishing.service.TriggerEventService;
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
 * Service implementation for configurable trigger event entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TriggerEventServiceImpl implements TriggerEventService {

    private final TriggerEventRepository triggerEventRepository;

    @Override
    @Transactional
    public TriggerEventDto createTriggerEvent(TriggerEventCreateRequest request) {
        String name = request.getName().trim();
        if (triggerEventRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Trigger event name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        TriggerEvent entity = TriggerEvent.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        TriggerEvent saved = triggerEventRepository.save(entity);
        log.info("Created TriggerEvent id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public TriggerEventDto updateTriggerEvent(String id, TriggerEventUpdateRequest request) {
        TriggerEvent entity = triggerEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger event not found"));
        String name = request.getName().trim();
        if (triggerEventRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Trigger event name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        TriggerEvent saved = triggerEventRepository.save(entity);
        log.info("Updated TriggerEvent id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteTriggerEvent(String id) {
        if (!triggerEventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trigger event not found");
        }
        triggerEventRepository.deleteById(id);
        log.info("Deleted TriggerEvent id={}", id);
    }

    @Override
    public TriggerEventDto getTriggerEventById(String id) {
        return triggerEventRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger event not found"));
    }

    @Override
    public List<TriggerEventDto> getTriggerEvents(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<TriggerEvent> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? triggerEventRepository.searchByNameWhereEffectiveActive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : triggerEventRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? triggerEventRepository.searchByNameWhereInactive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : triggerEventRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countTriggerEvents(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? triggerEventRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : triggerEventRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? triggerEventRepository.countWhereEffectiveActive()
                : triggerEventRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<TriggerEvent> defaults = triggerEventRepository.findAllByIsDefaultTrue();
        List<TriggerEvent> toSave = new ArrayList<>();
        for (TriggerEvent p : defaults) {
            if (keepId == null || !keepId.equals(p.getId())) {
                p.setIsDefault(false);
                toSave.add(p);
            }
        }
        if (!toSave.isEmpty()) {
            triggerEventRepository.saveAll(toSave);
        }
    }

    private TriggerEventDto toDto(TriggerEvent entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return TriggerEventDto.builder()
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
