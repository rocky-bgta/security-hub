package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.DataCaptureTypeController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.DataCaptureTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.DataCaptureTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.DataCaptureTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link DataCaptureTypeController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DataCaptureTypeControllerImpl implements DataCaptureTypeController {

    private final DataCaptureTypeService dataCaptureTypeService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<DataCaptureTypeDto>>>> getDataCaptureTypes(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<DataCaptureTypeDto> items = dataCaptureTypeService.getDataCaptureTypes(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = dataCaptureTypeService.countDataCaptureTypes(searchParam, isActive);

            AllResponseDto<List<DataCaptureTypeDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Data capture types retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting data capture types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve data capture types", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DataCaptureTypeDto>> getDataCaptureTypeById(String id) {
        try {
            DataCaptureTypeDto dto = dataCaptureTypeService.getDataCaptureTypeById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Data capture type retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting data capture type by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve data capture type", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DataCaptureTypeDto>> createDataCaptureType(
            DataCaptureTypeCreateRequest request) {
        DataCaptureTypeDto created = dataCaptureTypeService.createDataCaptureType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Data capture type created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<DataCaptureTypeDto>> updateDataCaptureType(
            String id, DataCaptureTypeUpdateRequest request) {
        DataCaptureTypeDto updated = dataCaptureTypeService.updateDataCaptureType(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Data capture type updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteDataCaptureType(String id) {
        dataCaptureTypeService.deleteDataCaptureType(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Data capture type deleted successfully", 200, null));
    }
}
