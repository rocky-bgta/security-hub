package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.UrgencyLevelCreateRequest;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.model.UrgencyLevel;
import com.aspire.asat.phishing.repository.UrgencyLevelRepository;
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
class UrgencyLevelServiceImplTest {

    @Mock
    private UrgencyLevelRepository urgencyLevelRepository;

    @InjectMocks
    private UrgencyLevelServiceImpl urgencyLevelService;

    @Test
    void createUrgencyLevelShouldCreateAndReturnDto() {
        UrgencyLevelCreateRequest request = UrgencyLevelCreateRequest.builder()
                .name("High")
                .description("Immediate action required")
                .displayOrder(1)
                .isDefault(true)
                .isActive(true)
                .build();

        when(urgencyLevelRepository.existsByNameIgnoreCase("High")).thenReturn(false);
        when(urgencyLevelRepository.findAllByIsDefaultTrue()).thenReturn(List.of());
        when(urgencyLevelRepository.save(any(UrgencyLevel.class))).thenAnswer(inv -> {
            UrgencyLevel entity = inv.getArgument(0);
            entity.setId("ul-1");
            return entity;
        });

        UrgencyLevelDto result = urgencyLevelService.createUrgencyLevel(request);

        assertEquals("ul-1", result.getId());
        assertEquals("High", result.getName());
        assertEquals(true, result.getIsDefault());
        verify(urgencyLevelRepository).save(any(UrgencyLevel.class));
    }

    @Test
    void createUrgencyLevelShouldRejectDuplicateName() {
        UrgencyLevelCreateRequest request = UrgencyLevelCreateRequest.builder()
                .name("High")
                .displayOrder(1)
                .build();
        when(urgencyLevelRepository.existsByNameIgnoreCase("High")).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class,
                () -> urgencyLevelService.createUrgencyLevel(request));
    }
}
