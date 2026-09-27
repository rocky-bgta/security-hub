package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.TriggerEventController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.TriggerEventCreateRequest;
import com.aspire.asat.phishing.dto.request.TriggerEventUpdateRequest;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.TriggerEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link TriggerEventController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TriggerEventControllerImpl implements TriggerEventController {

    private final TriggerEventService triggerEventService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<TriggerEventDto>>>> getTriggerEvents(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        try {
            List<TriggerEventDto> items = triggerEventService.getTriggerEvents(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = triggerEventService.countTriggerEvents(searchParam, isActive);
            AllResponseDto<List<TriggerEventDto>> response = new AllResponseDto<>(offset, pageSize, total, items);
            return ResponseEntity.ok(new ApiResponseDto<>("Trigger events retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting trigger events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve trigger events", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<TriggerEventDto>> getTriggerEventById(String id) {
        try {
            TriggerEventDto dto = triggerEventService.getTriggerEventById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Trigger event retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting trigger event by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve trigger event", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<TriggerEventDto>> createTriggerEvent(TriggerEventCreateRequest request) {
        TriggerEventDto created = triggerEventService.createTriggerEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Trigger event created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TriggerEventDto>> updateTriggerEvent(
            String id, TriggerEventUpdateRequest request) {
        TriggerEventDto updated = triggerEventService.updateTriggerEvent(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Trigger event updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteTriggerEvent(String id) {
        triggerEventService.deleteTriggerEvent(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Trigger event deleted successfully", 200, null));
    }
}
