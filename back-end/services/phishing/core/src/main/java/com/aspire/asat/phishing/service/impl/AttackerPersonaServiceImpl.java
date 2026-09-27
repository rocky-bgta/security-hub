package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.AttackerPersonaCreateRequest;
import com.aspire.asat.phishing.dto.request.AttackerPersonaUpdateRequest;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.AttackerPersona;
import com.aspire.asat.phishing.repository.AttackerPersonaRepository;
import com.aspire.asat.phishing.service.AttackerPersonaService;
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
 * Service implementation for configurable attacker persona entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttackerPersonaServiceImpl implements AttackerPersonaService {

    private final AttackerPersonaRepository attackerPersonaRepository;

    @Override
    @Transactional
    public AttackerPersonaDto createAttackerPersona(AttackerPersonaCreateRequest request) {
        String name = request.getName().trim();
        if (attackerPersonaRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Attacker persona name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        AttackerPersona entity = AttackerPersona.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        AttackerPersona saved = attackerPersonaRepository.save(entity);
        log.info("Created AttackerPersona id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public AttackerPersonaDto updateAttackerPersona(String id, AttackerPersonaUpdateRequest request) {
        AttackerPersona entity = attackerPersonaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attacker persona not found"));
        String name = request.getName().trim();
        if (attackerPersonaRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Attacker persona name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        AttackerPersona saved = attackerPersonaRepository.save(entity);
        log.info("Updated AttackerPersona id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAttackerPersona(String id) {
        if (!attackerPersonaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attacker persona not found");
        }
        attackerPersonaRepository.deleteById(id);
        log.info("Deleted AttackerPersona id={}", id);
    }

    @Override
    public AttackerPersonaDto getAttackerPersonaById(String id) {
        return attackerPersonaRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Attacker persona not found"));
    }

    @Override
    public List<AttackerPersonaDto> getAttackerPersonas(String searchParam, boolean isActive, int offset, int pageSize,
                                                        String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<AttackerPersona> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? attackerPersonaRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : attackerPersonaRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? attackerPersonaRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : attackerPersonaRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countAttackerPersonas(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? attackerPersonaRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : attackerPersonaRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? attackerPersonaRepository.countWhereEffectiveActive()
                : attackerPersonaRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<AttackerPersona> defaults = attackerPersonaRepository.findAllByIsDefaultTrue();
        List<AttackerPersona> toSave = new ArrayList<>();
        for (AttackerPersona p : defaults) {
            if (keepId == null || !keepId.equals(p.getId())) {
                p.setIsDefault(false);
                toSave.add(p);
            }
        }
        if (!toSave.isEmpty()) {
            attackerPersonaRepository.saveAll(toSave);
        }
    }

    private AttackerPersonaDto toDto(AttackerPersona entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return AttackerPersonaDto.builder()
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
