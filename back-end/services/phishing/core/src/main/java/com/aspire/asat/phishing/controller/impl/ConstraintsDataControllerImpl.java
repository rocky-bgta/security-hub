package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.ConstraintsDataController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.ConstraintsDataCreateRequest;
import com.aspire.asat.phishing.dto.request.ConstraintsDataUpdateRequest;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.ConstraintsDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link ConstraintsDataController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ConstraintsDataControllerImpl implements ConstraintsDataController {

    private final ConstraintsDataService constraintsDataService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ConstraintsDataDto>>>> getConstraintsData(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        try {
            List<ConstraintsDataDto> items = constraintsDataService.getConstraintsDataList(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = constraintsDataService.countConstraintsData(searchParam, isActive);
            AllResponseDto<List<ConstraintsDataDto>> response = new AllResponseDto<>(offset, pageSize, total, items);
            return ResponseEntity.ok(new ApiResponseDto<>("Constraints data retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting constraints data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve constraints data", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ConstraintsDataDto>> getConstraintsDataById(String id) {
        try {
            ConstraintsDataDto dto = constraintsDataService.getConstraintsDataById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Constraints data retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting constraints data by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve constraints data", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ConstraintsDataDto>> createConstraintsData(ConstraintsDataCreateRequest request) {
        ConstraintsDataDto created = constraintsDataService.createConstraintsData(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Constraints data created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ConstraintsDataDto>> updateConstraintsData(
            String id, ConstraintsDataUpdateRequest request) {
        ConstraintsDataDto updated = constraintsDataService.updateConstraintsData(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Constraints data updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteConstraintsData(String id) {
        constraintsDataService.deleteConstraintsData(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Constraints data deleted successfully", 200, null));
    }
}
