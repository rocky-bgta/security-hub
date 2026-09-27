package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.ToneCreateRequest;
import com.aspire.asat.phishing.dto.request.ToneUpdateRequest;
import com.aspire.asat.phishing.dto.response.ToneDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.Tone;
import com.aspire.asat.phishing.repository.ToneRepository;
import com.aspire.asat.phishing.service.ToneService;
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
 * Service implementation for configurable tone entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ToneServiceImpl implements ToneService {

    private final ToneRepository toneRepository;

    @Override
    @Transactional
    public ToneDto createTone(ToneCreateRequest request) {
        String name = request.getName().trim();
        if (toneRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Tone name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        Tone entity = Tone.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        Tone saved = toneRepository.save(entity);
        log.info("Created Tone id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public ToneDto updateTone(String id, ToneUpdateRequest request) {
        Tone entity = toneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tone not found"));
        String name = request.getName().trim();
        if (toneRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Tone name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        Tone saved = toneRepository.save(entity);
        log.info("Updated Tone id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteTone(String id) {
        if (!toneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tone not found");
        }
        toneRepository.deleteById(id);
        log.info("Deleted Tone id={}", id);
    }

    @Override
    public ToneDto getToneById(String toneId) {
        return toneRepository.findById(toneId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Tone not found"));
    }

    @Override
    public List<ToneDto> getTones(String searchParam, boolean isActive, int offset, int pageSize,
                                  String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<Tone> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? toneRepository.searchByNameWhereEffectiveActive(CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : toneRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? toneRepository.searchByNameWhereInactive(CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : toneRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countTones(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? toneRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : toneRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? toneRepository.countWhereEffectiveActive()
                : toneRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<Tone> defaults = toneRepository.findAllByIsDefaultTrue();
        List<Tone> toSave = new ArrayList<>();
        for (Tone t : defaults) {
            if (keepId == null || !keepId.equals(t.getId())) {
                t.setIsDefault(false);
                toSave.add(t);
            }
        }
        if (!toSave.isEmpty()) {
            toneRepository.saveAll(toSave);
        }
    }

    private ToneDto toDto(Tone entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return ToneDto.builder()
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
