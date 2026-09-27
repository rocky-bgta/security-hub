package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
import com.aspire.asat.phishing.dto.response.DeceptionLevelDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.LandingPageCategoryDto;
import com.aspire.asat.phishing.dto.response.PersonalizationLevelDto;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.CatalogDtoMapper;
import com.aspire.asat.phishing.model.DataCaptureType;
import com.aspire.asat.phishing.model.DeceptionLevel;
import com.aspire.asat.phishing.model.Difficulty;
import com.aspire.asat.phishing.model.LandingPageCategory;
import com.aspire.asat.phishing.model.PersonalizationLevel;
import com.aspire.asat.phishing.repository.DataCaptureTypeRepository;
import com.aspire.asat.phishing.repository.DeceptionLevelRepository;
import com.aspire.asat.phishing.repository.DifficultyRepository;
import com.aspire.asat.phishing.repository.LandingPageCategoryRepository;
import com.aspire.asat.phishing.repository.PersonalizationLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves optional catalog references from requests into embedded DTO snapshots.
 */
@Component
@RequiredArgsConstructor
public class CatalogReferenceResolver {

    private final LandingPageCategoryRepository landingPageCategoryRepository;
    private final DifficultyRepository difficultyRepository;
    private final DeceptionLevelRepository deceptionLevelRepository;
    private final PersonalizationLevelRepository personalizationLevelRepository;
    private final DataCaptureTypeRepository dataCaptureTypeRepository;
    private final CatalogDtoMapper catalogDtoMapper;

    public LandingPageCategoryDto resolveCategory(LandingPageCategoryDto input) {
        if (input == null) {
            return null;
        }
        if (!StringUtils.hasText(input.getId())) {
            throw new ServiceException("category.id is required when category is provided");
        }
        LandingPageCategory entity = landingPageCategoryRepository.findById(input.getId().trim())
                .orElseThrow(() -> new ServiceException("Invalid category id: " + input.getId()));
        if (Boolean.FALSE.equals(entity.getIsActive())) {
            throw new ServiceException("Landing page category is inactive: " + input.getId());
        }
        return catalogDtoMapper.toCategoryDto(entity);
    }

    public DifficultyDto resolveDifficulty(DifficultyDto input) {
        if (input == null) {
            return null;
        }
        if (!StringUtils.hasText(input.getId())) {
            throw new ServiceException("difficultyLevel.id is required when difficultyLevel is provided");
        }
        Difficulty entity = difficultyRepository.findById(input.getId().trim())
                .orElseThrow(() -> new ServiceException("Invalid difficulty id: " + input.getId()));
        if (Boolean.FALSE.equals(entity.getIsActive())) {
            throw new ServiceException("Difficulty is inactive: " + input.getId());
        }
        return catalogDtoMapper.toDifficultyDto(entity);
    }

    public DeceptionLevelDto resolveDeceptionLevel(DeceptionLevelDto input) {
        if (input == null) {
            return null;
        }
        if (!StringUtils.hasText(input.getId())) {
            throw new ServiceException("deceptionLevel.id is required when deceptionLevel is provided");
        }
        DeceptionLevel entity = deceptionLevelRepository.findById(input.getId().trim())
                .orElseThrow(() -> new ServiceException("Invalid deception level id: " + input.getId()));
        if (Boolean.FALSE.equals(entity.getIsActive())) {
            throw new ServiceException("Deception level is inactive: " + input.getId());
        }
        return catalogDtoMapper.toDeceptionLevelDto(entity);
    }

    public PersonalizationLevelDto resolvePersonalizationLevel(PersonalizationLevelDto input) {
        if (input == null) {
            return null;
        }
        if (!StringUtils.hasText(input.getId())) {
            throw new ServiceException("personalizationLevel.id is required when personalizationLevel is provided");
        }
        PersonalizationLevel entity = personalizationLevelRepository.findById(input.getId().trim())
                .orElseThrow(() -> new ServiceException("Invalid personalization level id: " + input.getId()));
        if (Boolean.FALSE.equals(entity.getIsActive())) {
            throw new ServiceException("Personalization level is inactive: " + input.getId());
        }
        return catalogDtoMapper.toPersonalizationLevelDto(entity);
    }

    public DataCaptureTypeDto resolveDataCaptureType(DataCaptureTypeDto input) {
        if (input == null) {
            return null;
        }
        if (!StringUtils.hasText(input.getId())) {
            throw new ServiceException("dataCaptureType.id is required when dataCaptureType is provided");
        }
        DataCaptureType entity = dataCaptureTypeRepository.findById(input.getId().trim())
                .orElseThrow(() -> new ServiceException("Invalid data capture type id: " + input.getId()));
        if (Boolean.FALSE.equals(entity.getIsActive())) {
            throw new ServiceException("Data capture type is inactive: " + input.getId());
        }
        return catalogDtoMapper.toDataCaptureTypeDto(entity);
    }
}
