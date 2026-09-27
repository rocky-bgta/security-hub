package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.ExpectedUserActionController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.ExpectedUserActionCreateRequest;
import com.aspire.asat.phishing.dto.request.ExpectedUserActionUpdateRequest;
import com.aspire.asat.phishing.dto.response.ExpectedUserActionDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.ExpectedUserActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link ExpectedUserActionController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ExpectedUserActionControllerImpl implements ExpectedUserActionController {

    private final ExpectedUserActionService expectedUserActionService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ExpectedUserActionDto>>>> getExpectedUserActions(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        try {
            List<ExpectedUserActionDto> items = expectedUserActionService.getExpectedUserActions(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = expectedUserActionService.countExpectedUserActions(searchParam, isActive);
            AllResponseDto<List<ExpectedUserActionDto>> response = new AllResponseDto<>(offset, pageSize, total, items);
            return ResponseEntity.ok(new ApiResponseDto<>("Expected user actions retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting expected user actions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve expected user actions", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExpectedUserActionDto>> getExpectedUserActionById(String id) {
        try {
            ExpectedUserActionDto dto = expectedUserActionService.getExpectedUserActionById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Expected user action retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting expected user action by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve expected user action", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExpectedUserActionDto>> createExpectedUserAction(
            ExpectedUserActionCreateRequest request) {
        ExpectedUserActionDto created = expectedUserActionService.createExpectedUserAction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Expected user action created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExpectedUserActionDto>> updateExpectedUserAction(
            String id, ExpectedUserActionUpdateRequest request) {
        ExpectedUserActionDto updated = expectedUserActionService.updateExpectedUserAction(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Expected user action updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteExpectedUserAction(String id) {
        expectedUserActionService.deleteExpectedUserAction(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Expected user action deleted successfully", 200, null));
    }
}
