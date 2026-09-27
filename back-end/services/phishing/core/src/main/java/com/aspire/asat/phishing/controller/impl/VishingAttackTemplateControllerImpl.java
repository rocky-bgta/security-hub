package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.VishingAttackTemplateController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingAttackTemplateDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.VishingAttackTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link VishingAttackTemplateController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class VishingAttackTemplateControllerImpl implements VishingAttackTemplateController {

    private final VishingAttackTemplateService vishingAttackTemplateService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<VishingAttackTemplateDto>>>> list(
            String searchParam, int offset, int pageSize, String sortBy, String sortOrder) {
        try {
            List<VishingAttackTemplateDto> items = vishingAttackTemplateService.list(
                    searchParam, offset, pageSize, sortBy, sortOrder);
            long total = vishingAttackTemplateService.count(searchParam);
            AllResponseDto<List<VishingAttackTemplateDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);
            return ResponseEntity.ok(
                    new ApiResponseDto<>("Vishing attack templates retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error listing vishing attack templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve vishing attack templates", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingAttackTemplateDto>> getById(String id) {
        try {
            VishingAttackTemplateDto dto = vishingAttackTemplateService.getById(id);
            return ResponseEntity.ok(
                    new ApiResponseDto<>("Vishing attack template retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting vishing attack template by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve vishing attack template", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingAttackTemplateDto>> create(
            VishingAttackTemplateCreateRequest request) {
        VishingAttackTemplateDto created = vishingAttackTemplateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Vishing attack template created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingAttackTemplateDto>> update(
            String id, VishingAttackTemplateUpdateRequest request) {
        VishingAttackTemplateDto updated = vishingAttackTemplateService.update(id, request);
        return ResponseEntity.ok(
                new ApiResponseDto<>("Vishing attack template updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> delete(String id) {
        vishingAttackTemplateService.delete(id);
        return ResponseEntity.ok(
                new ApiResponseDto<>("Vishing attack template deleted successfully", 200, null));
    }
}
