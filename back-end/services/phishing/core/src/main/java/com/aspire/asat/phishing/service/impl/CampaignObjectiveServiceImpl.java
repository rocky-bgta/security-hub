package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.CampaignObjectiveCreateRequest;
import com.aspire.asat.phishing.dto.request.CampaignObjectiveUpdateRequest;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.CampaignObjective;
import com.aspire.asat.phishing.repository.CampaignObjectiveRepository;
import com.aspire.asat.phishing.service.CampaignObjectiveService;
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
 * Service implementation for configurable campaign objective entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignObjectiveServiceImpl implements CampaignObjectiveService {

    private final CampaignObjectiveRepository campaignObjectiveRepository;

    @Override
    @Transactional
    public CampaignObjectiveDto createCampaignObjective(CampaignObjectiveCreateRequest request) {
        String name = request.getName().trim();
        if (campaignObjectiveRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Campaign objective name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        CampaignObjective entity = CampaignObjective.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        CampaignObjective saved = campaignObjectiveRepository.save(entity);
        log.info("Created CampaignObjective id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public CampaignObjectiveDto updateCampaignObjective(String id, CampaignObjectiveUpdateRequest request) {
        CampaignObjective entity = campaignObjectiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign objective not found"));
        String name = request.getName().trim();
        if (campaignObjectiveRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Campaign objective name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        CampaignObjective saved = campaignObjectiveRepository.save(entity);
        log.info("Updated CampaignObjective id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCampaignObjective(String id) {
        if (!campaignObjectiveRepository.existsById(id)) {
            throw new ResourceNotFoundException("Campaign objective not found");
        }
        campaignObjectiveRepository.deleteById(id);
        log.info("Deleted CampaignObjective id={}", id);
    }

    @Override
    public CampaignObjectiveDto getCampaignObjectiveById(String id) {
        return campaignObjectiveRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign objective not found"));
    }

    @Override
    public List<CampaignObjectiveDto> getCampaignObjectives(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<CampaignObjective> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? campaignObjectiveRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : campaignObjectiveRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? campaignObjectiveRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : campaignObjectiveRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countCampaignObjectives(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? campaignObjectiveRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : campaignObjectiveRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? campaignObjectiveRepository.countWhereEffectiveActive()
                : campaignObjectiveRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<CampaignObjective> defaults = campaignObjectiveRepository.findAllByIsDefaultTrue();
        List<CampaignObjective> toSave = new ArrayList<>();
        for (CampaignObjective o : defaults) {
            if (keepId == null || !keepId.equals(o.getId())) {
                o.setIsDefault(false);
                toSave.add(o);
            }
        }
        if (!toSave.isEmpty()) {
            campaignObjectiveRepository.saveAll(toSave);
        }
    }

    private CampaignObjectiveDto toDto(CampaignObjective entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return CampaignObjectiveDto.builder()
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
