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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogReferenceResolverTest {

    @Mock
    private LandingPageCategoryRepository landingPageCategoryRepository;
    @Mock
    private DifficultyRepository difficultyRepository;
    @Mock
    private DeceptionLevelRepository deceptionLevelRepository;
    @Mock
    private PersonalizationLevelRepository personalizationLevelRepository;
    @Mock
    private DataCaptureTypeRepository dataCaptureTypeRepository;
    @Mock
    private CatalogDtoMapper catalogDtoMapper;

    @InjectMocks
    private CatalogReferenceResolver resolver;

    @Test
    void resolveCategory_nullInput_returnsNull() {
        assertNull(resolver.resolveCategory(null));
    }

    @Test
    void resolveCategory_validActive_returnsSnapshot() {
        LandingPageCategory entity = LandingPageCategory.builder()
                .id("cat-1")
                .name("Business")
                .isActive(true)
                .build();
        LandingPageCategoryDto dto = LandingPageCategoryDto.builder().id("cat-1").name("Business").build();

        when(landingPageCategoryRepository.findById("cat-1")).thenReturn(Optional.of(entity));
        when(catalogDtoMapper.toCategoryDto(entity)).thenReturn(dto);

        LandingPageCategoryDto result = resolver.resolveCategory(
                LandingPageCategoryDto.builder().id("cat-1").build());

        assertEquals("cat-1", result.getId());
        assertEquals("Business", result.getName());
    }

    @Test
    void resolveCategory_inactive_throws() {
        when(landingPageCategoryRepository.findById("cat-1"))
                .thenReturn(Optional.of(LandingPageCategory.builder().id("cat-1").isActive(false).build()));

        assertThrows(ServiceException.class, () -> resolver.resolveCategory(
                LandingPageCategoryDto.builder().id("cat-1").build()));
    }

    @Test
    void resolveDifficulty_missingId_throws() {
        assertThrows(ServiceException.class, () -> resolver.resolveDifficulty(new DifficultyDto()));
    }

    @Test
    void resolveDeceptionLevel_nullInput_returnsNull() {
        assertNull(resolver.resolveDeceptionLevel(null));
    }

    @Test
    void resolveDeceptionLevel_validActive_returnsSnapshot() {
        DeceptionLevel entity = DeceptionLevel.builder().id("dec-1").name("Basic").isActive(true).build();
        DeceptionLevelDto dto = DeceptionLevelDto.builder().id("dec-1").name("Basic").build();

        when(deceptionLevelRepository.findById("dec-1")).thenReturn(Optional.of(entity));
        when(catalogDtoMapper.toDeceptionLevelDto(entity)).thenReturn(dto);

        DeceptionLevelDto result = resolver.resolveDeceptionLevel(
                DeceptionLevelDto.builder().id("dec-1").build());

        assertEquals("dec-1", result.getId());
        assertEquals("Basic", result.getName());
    }

    @Test
    void resolveDeceptionLevel_inactive_throws() {
        when(deceptionLevelRepository.findById("dec-1"))
                .thenReturn(Optional.of(DeceptionLevel.builder().id("dec-1").isActive(false).build()));

        assertThrows(ServiceException.class, () -> resolver.resolveDeceptionLevel(
                DeceptionLevelDto.builder().id("dec-1").build()));
    }

    @Test
    void resolvePersonalizationLevel_missingId_throws() {
        assertThrows(ServiceException.class, () -> resolver.resolvePersonalizationLevel(new PersonalizationLevelDto()));
    }

    @Test
    void resolvePersonalizationLevel_validActive_returnsSnapshot() {
        PersonalizationLevel entity = PersonalizationLevel.builder()
                .id("pl-1")
                .name("High")
                .isActive(true)
                .build();
        PersonalizationLevelDto dto = PersonalizationLevelDto.builder().id("pl-1").name("High").build();

        when(personalizationLevelRepository.findById("pl-1")).thenReturn(Optional.of(entity));
        when(catalogDtoMapper.toPersonalizationLevelDto(entity)).thenReturn(dto);

        PersonalizationLevelDto result = resolver.resolvePersonalizationLevel(
                PersonalizationLevelDto.builder().id("pl-1").build());

        assertEquals("pl-1", result.getId());
        assertEquals("High", result.getName());
    }

    @Test
    void resolveDataCaptureType_nullInput_returnsNull() {
        assertNull(resolver.resolveDataCaptureType(null));
    }

    @Test
    void resolveDataCaptureType_validActive_returnsSnapshot() {
        DataCaptureType entity = DataCaptureType.builder()
                .id("dct-1")
                .name("Credentials")
                .isActive(true)
                .build();
        DataCaptureTypeDto dto = DataCaptureTypeDto.builder().id("dct-1").name("Credentials").build();

        when(dataCaptureTypeRepository.findById("dct-1")).thenReturn(Optional.of(entity));
        when(catalogDtoMapper.toDataCaptureTypeDto(entity)).thenReturn(dto);

        DataCaptureTypeDto result = resolver.resolveDataCaptureType(
                DataCaptureTypeDto.builder().id("dct-1").build());

        assertEquals("dct-1", result.getId());
        assertEquals("Credentials", result.getName());
    }

    @Test
    void resolveDataCaptureType_inactive_throws() {
        when(dataCaptureTypeRepository.findById("dct-1"))
                .thenReturn(Optional.of(DataCaptureType.builder().id("dct-1").isActive(false).build()));

        assertThrows(ServiceException.class, () -> resolver.resolveDataCaptureType(
                DataCaptureTypeDto.builder().id("dct-1").build()));
    }
}
