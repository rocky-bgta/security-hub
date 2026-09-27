package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.AttackTechniqueCreateRequest;
import com.aspire.asat.phishing.dto.request.AttackTechniqueUpdateRequest;
import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.AttackTechnique;
import com.aspire.asat.phishing.repository.AttackTechniqueRepository;
import com.aspire.asat.phishing.service.AttackTechniqueService;
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
 * Service implementation for configurable attack technique entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttackTechniqueServiceImpl implements AttackTechniqueService {

    private final AttackTechniqueRepository attackTechniqueRepository;

    @Override
    @Transactional
    public AttackTechniqueDto createAttackTechnique(AttackTechniqueCreateRequest request) {
        String name = request.getName().trim();
        if (attackTechniqueRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Attack technique name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        AttackTechnique entity = AttackTechnique.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        AttackTechnique saved = attackTechniqueRepository.save(entity);
        log.info("Created AttackTechnique id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public AttackTechniqueDto updateAttackTechnique(String id, AttackTechniqueUpdateRequest request) {
        AttackTechnique entity = attackTechniqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attack technique not found"));
        String name = request.getName().trim();
        if (attackTechniqueRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Attack technique name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        AttackTechnique saved = attackTechniqueRepository.save(entity);
        log.info("Updated AttackTechnique id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAttackTechnique(String id) {
        if (!attackTechniqueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attack technique not found");
        }
        attackTechniqueRepository.deleteById(id);
        log.info("Deleted AttackTechnique id={}", id);
    }

    @Override
    public AttackTechniqueDto getAttackTechniqueById(String id) {
        return attackTechniqueRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Attack technique not found"));
    }

    @Override
    public List<AttackTechniqueDto> getAttackTechniques(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<AttackTechnique> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? attackTechniqueRepository.searchByNameWhereEffectiveActive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : attackTechniqueRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? attackTechniqueRepository.searchByNameWhereInactive(
                    CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : attackTechniqueRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countAttackTechniques(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? attackTechniqueRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : attackTechniqueRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? attackTechniqueRepository.countWhereEffectiveActive()
                : attackTechniqueRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<AttackTechnique> defaults = attackTechniqueRepository.findAllByIsDefaultTrue();
        List<AttackTechnique> toSave = new ArrayList<>();
        for (AttackTechnique p : defaults) {
            if (keepId == null || !keepId.equals(p.getId())) {
                p.setIsDefault(false);
                toSave.add(p);
            }
        }
        if (!toSave.isEmpty()) {
            attackTechniqueRepository.saveAll(toSave);
        }
    }

    private AttackTechniqueDto toDto(AttackTechnique entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return AttackTechniqueDto.builder()
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
