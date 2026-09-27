package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.TriggerEventCreateRequest;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
import com.aspire.asat.phishing.service.TriggerEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriggerEventControllerImplTest {

    @Mock
    private TriggerEventService triggerEventService;

    @InjectMocks
    private TriggerEventControllerImpl triggerEventController;

    @Test
    void createTriggerEventShouldReturnCreatedResponse() {
        TriggerEventCreateRequest request = TriggerEventCreateRequest.builder()
                .name("Password Reset")
                .displayOrder(1)
                .build();
        TriggerEventDto dto = TriggerEventDto.builder().id("te-1").name("Password Reset").build();
        when(triggerEventService.createTriggerEvent(request)).thenReturn(dto);

        ResponseEntity<ApiResponseDto<TriggerEventDto>> response = triggerEventController.createTriggerEvent(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Trigger event created successfully", response.getBody().getMessage());
        assertEquals("te-1", response.getBody().getData().getId());
    }
}
