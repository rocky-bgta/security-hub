package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.DifficultyCreateRequest;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.mapper.CatalogDtoMapper;
import com.aspire.asat.phishing.model.Difficulty;
import com.aspire.asat.phishing.repository.DifficultyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DifficultyServiceImplTest {

    @Mock
    private DifficultyRepository difficultyRepository;

    @Mock
    private CatalogDtoMapper catalogDtoMapper;

    @InjectMocks
    private DifficultyServiceImpl difficultyService;

    @Test
    void createDifficultyShouldCreateAndReturnDto() {
        DifficultyCreateRequest request = DifficultyCreateRequest.builder()
                .name("Advanced")
                .description("Hard to detect")
                .displayOrder(2)
                .isDefault(true)
                .isActive(true)
                .build();

        when(difficultyRepository.existsByNameIgnoreCase("Advanced")).thenReturn(false);
        when(difficultyRepository.findAllByIsDefaultTrue()).thenReturn(List.of());
        when(difficultyRepository.save(any(Difficulty.class))).thenAnswer(inv -> {
            Difficulty entity = inv.getArgument(0);
            entity.setId("diff-1");
            return entity;
        });
        when(catalogDtoMapper.toDifficultyDto(any(Difficulty.class))).thenAnswer(inv -> {
            Difficulty entity = inv.getArgument(0);
            return DifficultyDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .isDefault(entity.getIsDefault())
                    .build();
        });

        DifficultyDto result = difficultyService.createDifficulty(request);

        assertEquals("diff-1", result.getId());
        assertEquals("Advanced", result.getName());
        assertEquals(true, result.getIsDefault());
        verify(difficultyRepository).save(any(Difficulty.class));
    }

    @Test
    void createDifficultyShouldRejectDuplicateName() {
        DifficultyCreateRequest request = DifficultyCreateRequest.builder()
                .name("Advanced")
                .displayOrder(1)
                .build();
        when(difficultyRepository.existsByNameIgnoreCase("Advanced")).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class,
                () -> difficultyService.createDifficulty(request));
    }
}
