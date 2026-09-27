package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.ToneController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.ToneCreateRequest;
import com.aspire.asat.phishing.dto.request.ToneUpdateRequest;
import com.aspire.asat.phishing.dto.response.ToneDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.ToneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link ToneController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ToneControllerImpl implements ToneController {

    private final ToneService toneService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ToneDto>>>> getTones(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<ToneDto> items = toneService.getTones(searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = toneService.countTones(searchParam, isActive);

            AllResponseDto<List<ToneDto>> response = new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(new ApiResponseDto<>("Tones retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting tones", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve tones", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ToneDto>> getToneById(String id) {
        try {
            ToneDto dto = toneService.getToneById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Tone retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting tone by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve tone", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ToneDto>> createTone(ToneCreateRequest request) {
        ToneDto created = toneService.createTone(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Tone created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ToneDto>> updateTone(String id, ToneUpdateRequest request) {
        ToneDto updated = toneService.updateTone(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Tone updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteTone(String id) {
        toneService.deleteTone(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Tone deleted successfully", 200, null));
    }
}
