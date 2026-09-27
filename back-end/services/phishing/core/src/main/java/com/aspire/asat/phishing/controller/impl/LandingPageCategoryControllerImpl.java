package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.LandingPageCategoryController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.LandingPageCategoryCreateRequest;
import com.aspire.asat.phishing.dto.request.LandingPageCategoryUpdateRequest;
import com.aspire.asat.phishing.dto.response.LandingPageCategoryDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.LandingPageCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link LandingPageCategoryController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class LandingPageCategoryControllerImpl implements LandingPageCategoryController {

    private final LandingPageCategoryService landingPageCategoryService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<LandingPageCategoryDto>>>> getLandingPageCategories(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<LandingPageCategoryDto> items = landingPageCategoryService.getLandingPageCategories(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = landingPageCategoryService.countLandingPageCategories(searchParam, isActive);

            AllResponseDto<List<LandingPageCategoryDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Landing page categories retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting landing page categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve landing page categories", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<LandingPageCategoryDto>> getLandingPageCategoryById(String id) {
        try {
            LandingPageCategoryDto dto = landingPageCategoryService.getLandingPageCategoryById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Landing page category retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting landing page category by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve landing page category", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<LandingPageCategoryDto>> createLandingPageCategory(
            LandingPageCategoryCreateRequest request) {
        LandingPageCategoryDto created = landingPageCategoryService.createLandingPageCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Landing page category created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<LandingPageCategoryDto>> updateLandingPageCategory(
            String id, LandingPageCategoryUpdateRequest request) {
        LandingPageCategoryDto updated = landingPageCategoryService.updateLandingPageCategory(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Landing page category updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteLandingPageCategory(String id) {
        landingPageCategoryService.deleteLandingPageCategory(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Landing page category deleted successfully", 200, null));
    }
}
