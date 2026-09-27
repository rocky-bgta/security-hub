package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.EmotionalTriggerCreateRequest;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.model.EmotionalTrigger;
import com.aspire.asat.phishing.repository.EmotionalTriggerRepository;
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
class EmotionalTriggerServiceImplTest {

    @Mock
    private EmotionalTriggerRepository emotionalTriggerRepository;

    @InjectMocks
    private EmotionalTriggerServiceImpl emotionalTriggerService;

    @Test
    void createEmotionalTriggerShouldCreateAndReturnDto() {
        EmotionalTriggerCreateRequest request = EmotionalTriggerCreateRequest.builder()
                .name("Urgency")
                .description("Urgent pressure")
                .displayOrder(1)
                .isDefault(true)
                .isActive(true)
                .build();

        when(emotionalTriggerRepository.existsByNameIgnoreCase("Urgency")).thenReturn(false);
        when(emotionalTriggerRepository.findAllByIsDefaultTrue()).thenReturn(List.of());
        when(emotionalTriggerRepository.save(any(EmotionalTrigger.class))).thenAnswer(inv -> {
            EmotionalTrigger entity = inv.getArgument(0);
            entity.setId("et-1");
            return entity;
        });

        EmotionalTriggerDto result = emotionalTriggerService.createEmotionalTrigger(request);

        assertEquals("et-1", result.getId());
        assertEquals("Urgency", result.getName());
        assertEquals(true, result.getIsDefault());
        verify(emotionalTriggerRepository).save(any(EmotionalTrigger.class));
    }

    @Test
    void createEmotionalTriggerShouldRejectDuplicateName() {
        EmotionalTriggerCreateRequest request = EmotionalTriggerCreateRequest.builder()
                .name("Urgency")
                .displayOrder(1)
                .build();
        when(emotionalTriggerRepository.existsByNameIgnoreCase("Urgency")).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class,
                () -> emotionalTriggerService.createEmotionalTrigger(request));
    }
}
