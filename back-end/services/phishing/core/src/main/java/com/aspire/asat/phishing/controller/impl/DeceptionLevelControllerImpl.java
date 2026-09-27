package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.DeceptionLevelController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.DeceptionLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.DeceptionLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.DeceptionLevelDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.DeceptionLevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link DeceptionLevelController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DeceptionLevelControllerImpl implements DeceptionLevelController {

    private final DeceptionLevelService deceptionLevelService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<DeceptionLevelDto>>>> getDeceptionLevels(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<DeceptionLevelDto> items = deceptionLevelService.getDeceptionLevels(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = deceptionLevelService.countDeceptionLevels(searchParam, isActive);

            AllResponseDto<List<DeceptionLevelDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Deception levels retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting deception levels", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve deception levels", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeceptionLevelDto>> getDeceptionLevelById(String id) {
        try {
            DeceptionLevelDto dto = deceptionLevelService.getDeceptionLevelById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Deception level retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting deception level by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve deception level", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeceptionLevelDto>> createDeceptionLevel(
            DeceptionLevelCreateRequest request) {
        DeceptionLevelDto created = deceptionLevelService.createDeceptionLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Deception level created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeceptionLevelDto>> updateDeceptionLevel(
            String id, DeceptionLevelUpdateRequest request) {
        DeceptionLevelDto updated = deceptionLevelService.updateDeceptionLevel(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Deception level updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteDeceptionLevel(String id) {
        deceptionLevelService.deleteDeceptionLevel(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Deception level deleted successfully", 200, null));
    }
}
