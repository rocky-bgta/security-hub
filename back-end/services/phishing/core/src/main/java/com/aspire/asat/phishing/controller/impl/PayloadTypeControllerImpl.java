package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.PayloadTypeController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.request.PayloadTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.PayloadTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.PayloadTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link PayloadTypeController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PayloadTypeControllerImpl implements PayloadTypeController {

    private final PayloadTypeService payloadTypeService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<PayloadTypeDto>>>> getPayloadTypes(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder,
            PayloadTypeChannel channel) {
        try {
            List<PayloadTypeDto> items = payloadTypeService.getPayloadTypes(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder, channel);
            long total = payloadTypeService.countPayloadTypes(searchParam, isActive, channel);

            AllResponseDto<List<PayloadTypeDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Payload types retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting payload types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve payload types", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<PayloadTypeDto>> getPayloadTypeById(String id) {
        try {
            PayloadTypeDto dto = payloadTypeService.getPayloadTypeById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Payload type retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting payload type by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve payload type", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<PayloadTypeDto>> createPayloadType(PayloadTypeCreateRequest request) {
        PayloadTypeDto created = payloadTypeService.createPayloadType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Payload type created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<PayloadTypeDto>> updatePayloadType(String id, PayloadTypeUpdateRequest request) {
        PayloadTypeDto updated = payloadTypeService.updatePayloadType(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Payload type updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deletePayloadType(String id) {
        payloadTypeService.deletePayloadType(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Payload type deleted successfully", 200, null));
    }
}
