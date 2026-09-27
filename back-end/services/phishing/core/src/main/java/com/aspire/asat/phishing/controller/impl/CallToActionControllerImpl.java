package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.CallToActionController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.CallToActionCreateRequest;
import com.aspire.asat.phishing.dto.request.CallToActionUpdateRequest;
import com.aspire.asat.phishing.dto.response.CallToActionDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.CallToActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link CallToActionController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CallToActionControllerImpl implements CallToActionController {

    private final CallToActionService callToActionService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CallToActionDto>>>> getCallToActions(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<CallToActionDto> items = callToActionService.getCallToActions(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = callToActionService.countCallToActions(searchParam, isActive);

            AllResponseDto<List<CallToActionDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Call to actions retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting call to actions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve call to actions", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CallToActionDto>> getCallToActionById(String id) {
        try {
            CallToActionDto dto = callToActionService.getCallToActionById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Call to action retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting call to action by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve call to action", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CallToActionDto>> createCallToAction(CallToActionCreateRequest request) {
        CallToActionDto created = callToActionService.createCallToAction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Call to action created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CallToActionDto>> updateCallToAction(
            String id, CallToActionUpdateRequest request) {
        CallToActionDto updated = callToActionService.updateCallToAction(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Call to action updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteCallToAction(String id) {
        callToActionService.deleteCallToAction(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Call to action deleted successfully", 200, null));
    }
}
