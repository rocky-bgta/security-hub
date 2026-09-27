package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.EmotionalTriggerController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.EmotionalTriggerCreateRequest;
import com.aspire.asat.phishing.dto.request.EmotionalTriggerUpdateRequest;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.EmotionalTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link EmotionalTriggerController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class EmotionalTriggerControllerImpl implements EmotionalTriggerController {

    private final EmotionalTriggerService emotionalTriggerService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<EmotionalTriggerDto>>>> getEmotionalTriggers(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        try {
            List<EmotionalTriggerDto> items = emotionalTriggerService.getEmotionalTriggers(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = emotionalTriggerService.countEmotionalTriggers(searchParam, isActive);
            AllResponseDto<List<EmotionalTriggerDto>> response = new AllResponseDto<>(offset, pageSize, total, items);
            return ResponseEntity.ok(new ApiResponseDto<>("Emotional triggers retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting emotional triggers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve emotional triggers", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmotionalTriggerDto>> getEmotionalTriggerById(String id) {
        try {
            EmotionalTriggerDto dto = emotionalTriggerService.getEmotionalTriggerById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Emotional trigger retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting emotional trigger by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve emotional trigger", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmotionalTriggerDto>> createEmotionalTrigger(EmotionalTriggerCreateRequest request) {
        EmotionalTriggerDto created = emotionalTriggerService.createEmotionalTrigger(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Emotional trigger created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmotionalTriggerDto>> updateEmotionalTrigger(
            String id, EmotionalTriggerUpdateRequest request) {
        EmotionalTriggerDto updated = emotionalTriggerService.updateEmotionalTrigger(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Emotional trigger updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteEmotionalTrigger(String id) {
        emotionalTriggerService.deleteEmotionalTrigger(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Emotional trigger deleted successfully", 200, null));
    }
}
