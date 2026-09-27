package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.DifficultyCreateRequest;
import com.aspire.asat.phishing.dto.request.DifficultyUpdateRequest;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.mapper.CatalogDtoMapper;
import com.aspire.asat.phishing.model.Difficulty;
import com.aspire.asat.phishing.repository.DifficultyRepository;
import com.aspire.asat.phishing.service.DifficultyService;
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
 * Service implementation for configurable difficulty entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DifficultyServiceImpl implements DifficultyService {

    private final DifficultyRepository difficultyRepository;
    private final CatalogDtoMapper catalogDtoMapper;

    @Override
    @Transactional
    public DifficultyDto createDifficulty(DifficultyCreateRequest request) {
        String name = request.getName().trim();
        if (difficultyRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Difficulty name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        Difficulty entity = Difficulty.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .build();
        Difficulty saved = difficultyRepository.save(entity);
        log.info("Created Difficulty id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public DifficultyDto updateDifficulty(String id, DifficultyUpdateRequest request) {
        Difficulty entity = difficultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Difficulty not found"));
        String name = request.getName().trim();
        if (difficultyRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Difficulty name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        Difficulty saved = difficultyRepository.save(entity);
        log.info("Updated Difficulty id={}", id);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteDifficulty(String id) {
        if (!difficultyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Difficulty not found");
        }
        difficultyRepository.deleteById(id);
        log.info("Deleted Difficulty id={}", id);
    }

    @Override
    public DifficultyDto getDifficultyById(String id) {
        return difficultyRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Difficulty not found"));
    }

    @Override
    public List<DifficultyDto> getDifficulties(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<Difficulty> pageResult;
        if (isActive) {
            pageResult = (search != null)
                    ? difficultyRepository.searchByNameWhereEffectiveActive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : difficultyRepository.findWhereEffectiveActive(pageable);
        } else {
            pageResult = (search != null)
                    ? difficultyRepository.searchByNameWhereInactive(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : difficultyRepository.findWhereInactive(pageable);
        }

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countDifficulties(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return isActive
                    ? difficultyRepository.countSearchByNameWhereEffectiveActive(pattern)
                    : difficultyRepository.countSearchByNameWhereInactive(pattern);
        }
        return isActive
                ? difficultyRepository.countWhereEffectiveActive()
                : difficultyRepository.countWhereInactive();
    }

    private void clearDefaultsExcluding(String keepId) {
        List<Difficulty> defaults = difficultyRepository.findAllByIsDefaultTrue();
        List<Difficulty> toSave = new ArrayList<>();
        for (Difficulty difficulty : defaults) {
            if (keepId == null || !keepId.equals(difficulty.getId())) {
                difficulty.setIsDefault(false);
                toSave.add(difficulty);
            }
        }
        if (!toSave.isEmpty()) {
            difficultyRepository.saveAll(toSave);
        }
    }

    private DifficultyDto toDto(Difficulty entity) {
        return catalogDtoMapper.toDifficultyDto(entity);
    }
}
