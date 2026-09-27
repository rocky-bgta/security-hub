package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.UrgencyLevelController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.UrgencyLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.UrgencyLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.UrgencyLevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link UrgencyLevelController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UrgencyLevelControllerImpl implements UrgencyLevelController {

    private final UrgencyLevelService urgencyLevelService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<UrgencyLevelDto>>>> getUrgencyLevels(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<UrgencyLevelDto> items = urgencyLevelService.getUrgencyLevels(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = urgencyLevelService.countUrgencyLevels(searchParam, isActive);

            AllResponseDto<List<UrgencyLevelDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Urgency levels retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting urgency levels", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve urgency levels", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<UrgencyLevelDto>> getUrgencyLevelById(String id) {
        try {
            UrgencyLevelDto dto = urgencyLevelService.getUrgencyLevelById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Urgency level retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting urgency level by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve urgency level", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<UrgencyLevelDto>> createUrgencyLevel(UrgencyLevelCreateRequest request) {
        UrgencyLevelDto created = urgencyLevelService.createUrgencyLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Urgency level created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<UrgencyLevelDto>> updateUrgencyLevel(
            String id, UrgencyLevelUpdateRequest request) {
        UrgencyLevelDto updated = urgencyLevelService.updateUrgencyLevel(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Urgency level updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteUrgencyLevel(String id) {
        urgencyLevelService.deleteUrgencyLevel(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Urgency level deleted successfully", 200, null));
    }
}
